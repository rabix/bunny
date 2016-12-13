package org.rabix.backend.local.tes.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.mina.util.ConcurrentHashSet;
import org.rabix.backend.local.tes.client.TESHTTPClientException;
import org.rabix.backend.local.tes.client.TESHttpClient;
import org.rabix.backend.local.tes.model.TESDockerExecutor;
import org.rabix.backend.local.tes.model.TESJob;
import org.rabix.backend.local.tes.model.TESJobId;
import org.rabix.backend.local.tes.model.TESResources;
import org.rabix.backend.local.tes.model.TESState;
import org.rabix.backend.local.tes.model.TESTask;
import org.rabix.backend.local.tes.model.TESTaskParameter;
import org.rabix.backend.local.tes.model.TESVolume;
import org.rabix.backend.local.tes.service.TESServiceException;
import org.rabix.backend.local.tes.service.TESStorageService;
import org.rabix.backend.local.tes.service.TESStorageService.LocalFileStorage;
import org.rabix.backend.local.tes.service.TESStorageService.SharedFileStorage;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.FileValue.FileType;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.engine.EngineStubLocal;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.ExecutorStatusCallbackException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalTESExecutorServiceImpl implements ExecutorService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESExecutorServiceImpl.class);

  public final static String PYTHON_DEFAULT_DOCKER_IMAGE = "frolvlad/alpine-python2";
  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix/tes-command-line:v2";
  
  public final static String WORKING_DIR = "working_dir";
  public final static String STANDARD_OUT_LOG = "standard_out.log";
  public final static String STANDARD_ERROR_LOG = "standard_error.log";
  
  public final static String DEFAULT_PROJECT = "default";

  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";
  
  private TESHttpClient tesHttpClient;
  private TESStorageService storageService;

  private final Set<PendingResult> pendingResults = new ConcurrentHashSet<>();
  
  private final ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private final java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);
  
  private EngineStub<?, ?, ?> engineStub;
  
  private final Configuration configuration;
  private final ExecutorStatusCallback statusCallback;
  
  private class PendingResult {
    private Job job;
    private Future<TESJob> future;
    
    public PendingResult(Job job, Future<TESJob> future) {
      this.job = job;
      this.future = future;
    }
  }
  
  @Inject
  public LocalTESExecutorServiceImpl(final TESHttpClient tesHttpClient, final TESStorageService storageService, final ExecutorStatusCallback statusCallback, final Configuration configuration) {
    this.tesHttpClient = tesHttpClient;
    this.storageService = storageService;
    this.configuration = configuration;
    this.statusCallback = statusCallback;
    
    this.scheduledTaskChecker.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        String baseStorageDir = null;
        try {
          baseStorageDir = storageService.getStorageInfo().getBaseDir();
        } catch (TESServiceException e) {
          throw new RuntimeException("Failed to fetch base storage dir path", e);
        }
        
        for (Iterator<PendingResult> iterator = pendingResults.iterator(); iterator.hasNext();){
          PendingResult pending = (PendingResult) iterator.next();
          if (pending.future.isDone()) {
            try {
              TESJob tesJob = pending.future.get();
              if (tesJob.getState().equals(TESState.Complete)) {
                success(pending.job, tesJob, baseStorageDir);
              } else {
                fail(pending.job, tesJob);
              }
              iterator.remove();
            } catch (InterruptedException | ExecutionException e) {
              logger.error("Failed to retrieve TESJob", e);
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
          if (cause.getClass().equals(TESServiceException.class)) {
            Throwable subcause = cause.getCause();
            if (subcause != null) {
              if (subcause.getClass().equals(TESHTTPClientException.class)) {
                VerboseLogger.log("Failed to communicate with TES service");
                System.exit(-10);
              }
            }
          }
        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }
  
  @SuppressWarnings("unchecked")
  private void success(Job job, TESJob tesJob, final String baseStorageDir) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    Map<String, Object> result = (Map<String, Object>) FileValue.deserialize(JSONHelper.readMap(tesJob.getLogs().get(tesJob.getLogs().size()-1).getStdout())); // TODO change log fetching
    
    final Job finalJob = job;
    try {
      result = (Map<String, Object>) FileValueHelper.updateFileValues(result, new FileTransformer() {
        @Override
        public FileValue transform(FileValue fileValue) throws BindingException {
          String location = fileValue.getPath();
          if (location.startsWith(TESStorageService.DOCKER_PATH_PREFIX)) {
            location = Paths.get(finalJob.getId() , location.substring(TESStorageService.DOCKER_PATH_PREFIX.length() + 1)).toString();
          }
          if (!location.startsWith(File.pathSeparator)) {
            location = Paths.get(baseStorageDir, location).toString();
          }
          fileValue.setPath(location);
          fileValue.setLocation(location);
          return fileValue;
        }
      });
    } catch (BindingException e) {
      logger.error("Failed to process output files", e);
      throw new RuntimeException("Failed to process output files", e);
    }
    job = Job.cloneWithOutputs(job, result);
    job = Job.cloneWithMessage(job, "Success");
    try {
      job = statusCallback.onJobCompleted(job);
    } catch (ExecutorStatusCallbackException e1) {
      logger.warn("Failed to execute statusCallback: {}", e1);
    }
    engineStub.send(job);
  }
  
  private void fail(Job job, TESJob tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.FAILED);
    try {
      job = statusCallback.onJobFailed(job);
    } catch (ExecutorStatusCallbackException e) {
      logger.warn("Failed to execute statusCallback: {}", e);
    }
    engineStub.send(job);
  }
  
  @Override
  public void initialize(Backend backend) {
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
  }

  @Override
  public void start(Job job, String contextId) {
    pendingResults.add(new PendingResult(job, taskPoolExecutor.submit(new TaskRunCallable(job))));
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
  
  private File createJobDir(SharedFileStorage sharedFileStorage, Job job) {
    File jobDir = new File(sharedFileStorage.getBaseDir(), job.getId());
    if (!jobDir.exists()) {
      jobDir.mkdirs();
    }
    return jobDir;
  }
  
  private File createWorkingDir(SharedFileStorage sharedFileStorage, Job job) {
    File workingDir = new File(sharedFileStorage.getBaseDir(), getWorkingDirRelativePath(job));
    if (!workingDir.exists()) {
      workingDir.mkdirs();
    }
    return workingDir;
  }
  
  private String getWorkingDirRelativePath(Job job) {
    return Paths.get(job.getId(), WORKING_DIR).toString();
  }
  
  public class TaskRunCallable implements Callable<TESJob> {

    private Job job;
    
    public TaskRunCallable(Job job) {
      this.job = job;
    }
    
    @Override
    public TESJob call() throws Exception {
      try {
        final SharedFileStorage sharedFileStorage = storageService.getStorageInfo();

        LocalFileStorage localFileStorage = new LocalFileStorage(configuration.getString("backend.execution.directory"));
        job = storageService.stageInputFiles(job, localFileStorage, sharedFileStorage);

        List<TESTaskParameter> inputs = new ArrayList<>();
        inputs.add(new TESTaskParameter("mount", null, "", TESStorageService.DOCKER_PATH_PREFIX, FileType.Directory.name(), true));
        
        job = FileValueHelper.mapInputFilePaths(job, new FilePathMapper() {
          @Override
          public String map(String path, Map<String, Object> config) throws FileMappingException {
            return path.replace(sharedFileStorage.getBaseDir(), TESStorageService.DOCKER_PATH_PREFIX);
          }
        });
        
        Bindings bindings = BindingsFactory.create(job);
        
        createWorkingDir(sharedFileStorage, job);
        File jobDir = createJobDir(sharedFileStorage, job);

        String workingDirRelativePath = getWorkingDirRelativePath(job);
        inputs.add(new TESTaskParameter(WORKING_DIR, null, workingDirRelativePath, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString(), FileType.Directory.name(), false));

        File jobFile = new File(jobDir, "job.json");
        FileUtils.writeStringToFile(jobFile, JSONHelper.writeObject(job));
        inputs.add(new TESTaskParameter("job.json", null, Paths.get(job.getId(), "job.json").toString(), Paths.get(TESStorageService.DOCKER_PATH_PREFIX, "job.json").toString(), FileType.File.name(), true));
        
        List<TESTaskParameter> outputs = new ArrayList<>();
        outputs.add(new TESTaskParameter(WORKING_DIR, null, workingDirRelativePath, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString(), FileType.Directory.name(), false));    
        if (!bindings.isSelfExecutable(job)) {
          outputs.add(new TESTaskParameter("command.sh", null, Paths.get(job.getId(), "command.sh").toString(), Paths.get(TESStorageService.DOCKER_PATH_PREFIX, "command.sh").toString(), FileType.File.name(), false));
          outputs.add(new TESTaskParameter("environment.sh", null, Paths.get(job.getId(), "environment.sh").toString(), Paths.get(TESStorageService.DOCKER_PATH_PREFIX, "environment.sh").toString(), FileType.File.name(), false));
        }
        
        List<String> firstCommandLineParts = new ArrayList<>();
        firstCommandLineParts.add("/usr/share/rabix-tes-command-line/rabix");
        firstCommandLineParts.add("-j");
        firstCommandLineParts.add(Paths.get(TESStorageService.DOCKER_PATH_PREFIX, "job.json").toString());
        firstCommandLineParts.add("-w");
        firstCommandLineParts.add(Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString());
        firstCommandLineParts.add("-m");
        firstCommandLineParts.add("initialize");
        
        List<TESDockerExecutor> dockerExecutors = new ArrayList<>();
        String standardOutLog = Paths.get(TESStorageService.DOCKER_PATH_PREFIX, STANDARD_OUT_LOG).toString();
        String standardErrorLog = Paths.get(TESStorageService.DOCKER_PATH_PREFIX, STANDARD_ERROR_LOG).toString();
        
        dockerExecutors.add(new TESDockerExecutor(BUNNY_COMMAND_LINE_DOCKER_IMAGE, firstCommandLineParts, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString(), null, standardOutLog, standardErrorLog));
        
        List<Requirement> combinedRequirements = new ArrayList<>();
        combinedRequirements.addAll(bindings.getHints(job));
        combinedRequirements.addAll(bindings.getRequirements(job));

        DockerContainerRequirement dockerContainerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
        String imageId = null;
        if (dockerContainerRequirement == null) {
          imageId = PYTHON_DEFAULT_DOCKER_IMAGE;
        } else {
          imageId = dockerContainerRequirement.getDockerPull();
        }
        
        if (!bindings.isSelfExecutable(job)) {
          List<String> secondCommandLineParts = new ArrayList<>();
          secondCommandLineParts.add("/bin/sh");
          secondCommandLineParts.add("../command.sh");

          CommandLine commandLine = bindings.buildCommandLineObject(job, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toFile(), new FilePathMapper() {
            @Override
            public String map(String path, Map<String, Object> config) throws FileMappingException {
              return path;
            }
          });
          
          String commandLineToolStdout = commandLine.getStandardOut();
          if (commandLineToolStdout != null && !commandLineToolStdout.startsWith(File.pathSeparator)) {
            commandLineToolStdout = Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR, commandLineToolStdout).toString();
          }

          String commandLineToolErrLog = commandLine.getStandardError();
          String commandLineStandardErrLog = Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR, commandLineToolErrLog != null ? commandLineToolErrLog : DEFAULT_COMMAND_LINE_TOOL_ERR_LOG).toString();
          
          dockerExecutors.add(new TESDockerExecutor(imageId, secondCommandLineParts, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString(), null, commandLineToolStdout, commandLineStandardErrLog));
        }
        
        List<String> thirdCommandLineParts = new ArrayList<>();
        thirdCommandLineParts.add("/usr/share/rabix-tes-command-line/rabix");
        thirdCommandLineParts.add("-j");
        thirdCommandLineParts.add(Paths.get(TESStorageService.DOCKER_PATH_PREFIX, "job.json").toString());
        thirdCommandLineParts.add("-w");
        thirdCommandLineParts.add(Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString());
        thirdCommandLineParts.add("-m");
        thirdCommandLineParts.add("finalize");
        
        dockerExecutors.add(new TESDockerExecutor(BUNNY_COMMAND_LINE_DOCKER_IMAGE, thirdCommandLineParts, Paths.get(TESStorageService.DOCKER_PATH_PREFIX, WORKING_DIR).toString(), null, standardOutLog, standardErrorLog));
        
        List<TESVolume> volumes = new ArrayList<>();
        volumes.add(new TESVolume("vol_work", 1, null, TESStorageService.DOCKER_PATH_PREFIX));
        TESResources resources = new TESResources(null, false, null, volumes, null);

        TESTask task = new TESTask(job.getName(), DEFAULT_PROJECT, null, inputs, outputs, resources, job.getId(), dockerExecutors);
        
        TESJobId tesJobId = tesHttpClient.runTask(task);

        TESJob tesJob = null;
        do {
          Thread.sleep(1000L);
          tesJob = tesHttpClient.getJob(tesJobId);
          if (tesJob == null) {
            throw new TESServiceException("TESJob is not created. JobId = " + job.getId());
          }
        } while(!isFinished(tesJob));
        return tesJob;
      } catch (IOException e) {
        logger.error("Failed to write files to SharedFileStorage", e);
        throw new TESServiceException("Failed to write files to SharedFileStorage", e);
      } catch (TESHTTPClientException e) {
        logger.error("Failed to submit Job to TES", e);
        throw new TESServiceException("Failed to submit Job to TES", e);
      } catch (BindingException e) {
        logger.error("Failed to use Bindings", e);
        throw new TESServiceException("Failed to use Bindings", e);
      }
    }
    
    private boolean isFinished(TESJob tesJob) {
      return tesJob.getState().equals(TESState.Canceled) || 
          tesJob.getState().equals(TESState.Complete) || 
          tesJob.getState().equals(TESState.Error) || 
          tesJob.getState().equals(TESState.SystemError);
    }
    
  }
  
  @Override
  public void stop(List<String> ids, String contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void free(String rootId, Map<String, Object> config) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isRunning(String id, String contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public Map<String, Object> getResult(String id, String contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isStopped() {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public JobStatus findStatus(String id, String contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

}
