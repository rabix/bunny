package org.rabix.backend.slurm.service;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.slurm.SlurmServiceException;
import org.rabix.backend.slurm.client.SlurmClient;
import org.rabix.backend.slurm.client.SlurmClientException;
import org.rabix.backend.slurm.model.SlurmJob;
import org.rabix.backend.slurm.model.SlurmState;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlurmWorkerServiceImpl implements WorkerService {

    private final static Logger logger = LoggerFactory.getLogger(SlurmWorkerServiceImpl.class);
    private final static String TYPE = "SLURM";

    @BindingAnnotation
    @Target({ java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD })
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface SlurmWorker {
    }

    private Set<PendingResult> pendingResults = Collections.newSetFromMap(new ConcurrentHashMap<PendingResult, Boolean>());

    private final ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
    private final java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);

    private EngineStub<?, ?, ?> engineStub;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    @Inject
    private Configuration configuration;
    @Inject
    private WorkerStatusCallback statusCallback;
    @Inject
    private SlurmStorageService storageService;
    @Inject
    private SlurmClient slurmClient;
    @Inject
    private SlurmJobService slurmJobService;

    private class PendingResult {
        private Job job;
        private Future<SlurmJob> future;

        public PendingResult(Job job, Future<SlurmJob> future) {
            this.job = job;
            this.future = future;
        }
    }

    public SlurmWorkerServiceImpl(){
    }

    private void success(Job job) {
        String rootDir = configuration.getString("backend.execution.directory");
        job = Job.cloneWithStatus(job, Job.JobStatus.COMPLETED);
        try {
            FilePathMapper filePathMapper = (String path, Map<String, Object> config) -> path.startsWith("/") ? path : rootDir + "/" + path;
            job = FileValueHelper.mapInputFilePaths(job, filePathMapper);
            Bindings bindings = BindingsFactory.create(job);
            File baseDir = new File(rootDir + "/" + job.getRootId() + "/" + job.getName().replace(".", "/"));
            File workingDir = null;
            for (File file: baseDir.listFiles()){
                if (file.isDirectory()){
                    workingDir = new File(file, "root");
                }
            }
            if (workingDir == null) System.exit(14);
            job = bindings.preprocess(job, workingDir, null);
            job = bindings.postprocess(job, workingDir, null, null);
        } catch (BindingException e) {
            logger.error("Failed to postprocess job", e);
        }
        engineStub.send(job);
    }

    private void fail(Job job) {
        job = Job.cloneWithStatus(job, Job.JobStatus.FAILED);
        try {
            job = statusCallback.onJobFailed(job);
        } catch (WorkerStatusCallbackException e) {
            logger.warn("Failed to execute statusCallback: {}", e);
        }
        engineStub.send(job);
    }

    @Override
    public void start(Backend backend) {
        try {
            switch (backend.getType()) {
                case LOCAL:
                    engineStub = new EngineStubLocal((BackendLocal) backend, this, configuration);
                    break;
                default:
                    throw new TransportPluginException("Backend " + backend.getType() + " is not supported.");
            }
            engineStub.start();
        } catch (TransportPluginException e) {
            logger.error("Failed to initialize Executor", e);
            throw new RuntimeException("Failed to initialize Executor", e);
        }
        this.scheduledTaskChecker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Iterator<PendingResult> iterator = pendingResults.iterator(); iterator.hasNext(); ) {
                    PendingResult pending = iterator.next();
                    if (pending.future.isDone()) {
                        try {
                            SlurmJob slurmJob = pending.future.get();
                            if (slurmJob.getState().equals(SlurmState.Completed)) {
                                success(pending.job);
                            } else {
                                fail(pending.job);
                            }
                            iterator.remove();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Failed to retrieve SlurmJob", e);
                            handleException(e);
                            iterator.remove();
                        }
                    }
                }
            }

            /**
             * Basic exception handling
             */
            private void handleException(Exception e) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    if (cause.getClass().equals(SlurmServiceException.class)) {
                        Throwable subcause = cause.getCause();
                        if (subcause != null) {
                            if (subcause.getClass().equals(SlurmClientException.class)) {
                                VerboseLogger.log("Failed to communicate with SLURM");
                                System.exit(-10);
                            }
                        }
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Job job, UUID contextId) {
        pendingResults.add(new PendingResult(job, taskPoolExecutor.submit(new TaskRunCallable(job))));
    }


    public class TaskRunCallable implements Callable<SlurmJob> {

        private Job job;

        public TaskRunCallable(Job job) {
            this.job = job;
        }

        @Override
        public SlurmJob call() throws Exception {
            try {
                String rootDir = configuration.getString("backend.execution.directory");
                File workingDir = new File(rootDir + "/" + job.getRootId() + "/" + job.getName().replace(".", "/"));
                workingDir.mkdir();
                Bindings bindings = BindingsFactory.create(job);
                FilePathMapper filePathMapper = (String path, Map<String, Object> config) -> path.startsWith("/") ? path : rootDir + "/" + path;
                job = FileValueHelper.mapInputFilePaths(job, filePathMapper);
                job = bindings.preprocess(job, storageService.stagingPath(job.getRootId().toString(), job.getName()).toFile(), null);

                List<Requirement> combinedRequirements = new ArrayList<>();
                combinedRequirements.addAll(bindings.getHints(job));
                combinedRequirements.addAll(bindings.getRequirements(job));
                stageFileRequirements(workingDir, combinedRequirements);
                // Write job.json file
                FileUtils.writeStringToFile(
                        storageService.stagingPath(job.getRootId().toString(), job.getName(), "inputs", "job.json").toFile(),
                        JSONHelper.writeObject(job)
                );


                String slurmJobId = slurmClient.runJob(job, workingDir);
                slurmJobService.save(job.getId(), slurmJobId);
                SlurmJob slurmJob;
                do {
                    Thread.sleep(1000L);
                    slurmJob = slurmClient.getJob(slurmJobId);
                    if (slurmJob == null) {
                        throw new SlurmServiceException("SlurmJob is not created. JobId = " + job.getId());
                    }
                } while (!slurmJob.isFinished());
                return slurmJob;
            } catch (IOException e) {
                logger.error("Failed to write files to SharedFileStorage", e);
                throw new SlurmServiceException("Failed to write files to SharedFileStorage", e);
            } catch (SlurmServiceException e) {
                logger.error("Failed to submit Job to SLURM", e);
                throw new SlurmServiceException("Failed to submit Job to Slurm", e);
            } catch (BindingException e) {
                logger.error("Failed to use Bindings", e);
                throw new SlurmServiceException("Failed to use Bindings", e);
            }
        }

    }

    private void stageFileRequirements(File workingDir, List<Requirement> requirements) throws BindingException {
        try {
            FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
            if (fileRequirementResource == null) {
                return;
            }

            List<FileRequirement.SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
            if (fileRequirements == null) {
                return;
            }
            for (FileRequirement.SingleFileRequirement fileRequirement : fileRequirements) {
                logger.info("Process file requirement {}", fileRequirement);

                File destinationFile = new File(workingDir, fileRequirement.getFilename());
                if (fileRequirement instanceof FileRequirement.SingleTextFileRequirement) {
                    FileUtils.writeStringToFile(destinationFile, ((FileRequirement.SingleTextFileRequirement) fileRequirement).getContent());
                    continue;
                }
                if (fileRequirement instanceof FileRequirement.SingleInputFileRequirement || fileRequirement instanceof FileRequirement.SingleInputDirectoryRequirement) {
                    String path = ((FileRequirement.SingleInputFileRequirement) fileRequirement).getContent().getPath();
                    File file = new File(path);
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.isFile()) {
                        FileUtils.copyFile(file, destinationFile);
                    } else {
                        FileUtils.copyDirectory(file, destinationFile);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to process file requirements.", e);
            throw new BindingException("Failed to process file requirements.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Requirement> T getRequirement(List<Requirement> requirements, Class<T> clazz) {
        for (Requirement requirement : requirements) {
            if (requirement.getClass().equals(clazz)) {
                return (T) requirement;
            }
        }
        return null;
    }

    @Override
    public void cancel(List<UUID> ids, UUID contextId){
        logger.debug("stop(ids={})", ids);

        String slurmJobIds = "";
        for (UUID id : ids) {
            String slurmJobId = slurmJobService.getSlurmJob(id);
            slurmJobIds += " " + slurmJobId;
        }
        String command = "scancel " + slurmJobIds;
        SlurmClient.runCommand(command);
    }

    @Override
    public void freeResources(UUID rootId, Map<String, Object> config) {
        throw new NotImplementedException("This method is not implemented");
    }

    @Override
    public void shutdown(Boolean stopEverything) {
        stopped.set(true);
    }

    @Override
    public boolean isRunning(UUID id, UUID contextId) {
        logger.debug("isRunning(id={})", id);

        String slurmJobId = slurmJobService.getSlurmJob(id);
        SlurmJob slurmJob = null;
        try {
            slurmJob = slurmClient.getJob(slurmJobId);
        } catch (SlurmClientException e) {
            e.printStackTrace();
        }
        if (!slurmJob.isFinished()){
            logger.info("Command line tool {} is running. The status is {}", id, slurmJob.getJobStatus());
            return true;
        }
        logger.info("Command line tool {} is not running. The status is {}", id, slurmJob.getJobStatus());
        return false;
    }

    @Override
    public Map<String, Object> getResult(UUID id, UUID contextId) {
        throw new NotImplementedException("This method is not implemented");
    }

    @Override
    public boolean isStopped() {
       return stopped.get();
    }

    @Override
    public Job.JobStatus findStatus(UUID id, UUID contextId) {
        String slurmJobId = slurmJobService.getSlurmJob(id);
        SlurmJob slurmJob = null;
        try {
            slurmJob = slurmClient.getJob(slurmJobId);
        } catch (SlurmClientException e) {
            e.printStackTrace();
        }
        return SlurmJob.convertToJobStatus(slurmJob.getJobStatus());
    }

    @Override
    public String getType() {
        return TYPE;
    }


}
