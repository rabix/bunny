package org.rabix.backend.tes.service.impl;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.tes.client.TESHTTPClientException;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.model.TESDockerExecutor;
import org.rabix.backend.tes.model.TESJob;
import org.rabix.backend.tes.model.TESJobId;
import org.rabix.backend.tes.model.TESResources;
import org.rabix.backend.tes.model.TESState;
import org.rabix.backend.tes.model.TESTask;
import org.rabix.backend.tes.model.TESTaskParameter;
import org.rabix.backend.tes.model.TESVolume;
import org.rabix.backend.tes.service.TESServiceException;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

public class LocalTESWorkerServiceImpl implements WorkerService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESWorkerServiceImpl.class);

  private final static String TYPE = "TES";
  
  @BindingAnnotation
  @Target({ java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD })
  @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public static @interface TESWorker {
  }
  
  public final static String PYTHON_DEFAULT_DOCKER_IMAGE = "frolvlad/alpine-python2";
  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix-tes-cli";
//  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix/tes-command-line:v2";
  public final static String DEFAULT_PROJECT = "default";
  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";

  public static final String workingDirTes = "/mnt/working_dir/";
  public static final String inputsTes = "/mnt/inputs/";
  
  @Inject
  private TESHttpClient tesHttpClient;
  @Inject
  private TESStorageService storage;

  private Set<PendingResult> pendingResults = new ConcurrentHashSet<>();
  
  private ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);
  
  private EngineStub<?, ?, ?> engineStub;
  
  @Inject
  private Configuration configuration;
  @Inject
  private WorkerStatusCallback statusCallback;
  
  private class PendingResult {
    private Job job;
    private Future<TESJob> future;
    
    public PendingResult(Job job, Future<TESJob> future) {
      this.job = job;
      this.future = future;
    }
  }
  
  public LocalTESWorkerServiceImpl() {
  }
  
  @SuppressWarnings("unchecked")
  private void success(Job job, TESJob tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    try {
      Bindings bindings = BindingsFactory.create(job);
      String rootDir = configuration.getString("backend.execution.directory");
      File workingDir = new File(rootDir + "/" + job.getRootId() + "/" + job.getName().replace(".", "/"));
      job = bindings.postprocess(job, workingDir, HashAlgorithm.SHA1, null);
      //job = FileValueHelper.mapInputFilePaths(job,   (String path, Map<String, Object> config) -> path.replaceAll(workingDirTes, "").replaceAll(inputsTes, "")); //usualy not needed, but maybe
    } catch (BindingException e) {
      logger.error("Failed to postprocess job", e);
    }
    engineStub.send(job);
  }
  
  private void fail(Job job, TESJob tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.FAILED);
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
        for (Iterator<PendingResult> iterator = pendingResults.iterator(); iterator.hasNext();){
          PendingResult pending = (PendingResult) iterator.next();
          if (pending.future.isDone()) {
            try {
              TESJob tesJob = pending.future.get();
              if (tesJob.getState().equals(TESState.Complete)) {
                success(pending.job, tesJob);
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

  public void submit(Job job, UUID contextId) {
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

  
  public class TaskRunCallable implements Callable<TESJob> {

    private Job job;
    
    public TaskRunCallable(Job job) {
      this.job = job;
    }
    private void addInputFile(String name, Object input, List<TESTaskParameter> inputs) {
      if (!(input instanceof FileValue)) {
        if (input instanceof List) {
          List list = (List) input;
          for (int i = 0; i < list.size(); i++) {
            addInputFile(name + i, list.get(i), inputs);
          }
        }
        return;
      }
      FileValue file = (FileValue) input;
      inputs.add(
          new TESTaskParameter(name, "", "file://" + file.getPath(), inputsTes + file.getPath(), file instanceof DirectoryValue ? "Directory" : "File", false));
      if (file.getSecondaryFiles() != null)
        for (int i = 0; i < file.getSecondaryFiles().size(); i++) {
          addInputFile(name + i, file.getSecondaryFiles().get(i), inputs);
        }
    }

    @Override
    public TESJob call() throws Exception {
      try {
        String rootDir = configuration.getString("backend.execution.directory");
        File workingDir = new File(rootDir + "/" + job.getRootId() + "/" + job.getName().replace(".", "/"));
        workingDir.mkdir();
        String workPath = "file://" + workingDir.getAbsolutePath() + "/";

        FilePathMapper tesMapper = (String path, Map<String, Object> config) -> path.startsWith("/mnt") ? path : inputsTes + path; //dumb checks, need to use storageconfigs in future
        FilePathMapper filePathMapper = (String path, Map<String, Object> config) -> path.startsWith("/") ? path : rootDir + "/" + path;
        
        List<TESTaskParameter> inputs = new ArrayList<>();
        List<TESTaskParameter> outputs = new ArrayList<>();
        List<TESVolume> volumes = new ArrayList<>();
        List<TESDockerExecutor> commands = new ArrayList<>();

        Bindings bindings = BindingsFactory.create(job);
        job = FileValueHelper.mapInputFilePaths(job, filePathMapper);
        job = bindings.preprocess(job, storage.stagingPath(job.getRootId().toString(), job.getName()).toFile(), null);
        
        outputs.add(new TESTaskParameter("directory", "", workPath, workingDirTes, "Directory", true));

        List<Requirement> combinedRequirements = new ArrayList<>();
        combinedRequirements.addAll(bindings.getHints(job));
        combinedRequirements.addAll(bindings.getRequirements(job));
        FileRequirement requirement = getRequirement(combinedRequirements, FileRequirement.class);
        if (requirement != null) {
          inputs.addAll(requirement.getFileRequirements().stream().map(r -> {
            return new TESTaskParameter(r.getFilename(), "", workPath + r.getFilename(), workingDirTes + r.getFilename(), "File", true);
          }).collect(Collectors.toList()));
        }
        
        job.getInputs().entrySet().stream().forEach(e -> { //should be changed to use storage service, first should maybe look into preserving location in our files after mapping
          Object v = e.getValue();
          if (v instanceof Map) {
            ((Map<String, Object>) v).entrySet().stream().forEach(entry -> {
              addInputFile(entry.getKey(), entry.getValue(), inputs);
            });
          } else {
            addInputFile(e.getKey(), v, inputs);
          }
        });
        if (!bindings.isSelfExecutable(job)) {
          DockerContainerRequirement dockerContainerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
          String imageId;
          if (dockerContainerRequirement == null) {
            imageId = PYTHON_DEFAULT_DOCKER_IMAGE;
          } else {
            imageId = dockerContainerRequirement.getDockerPull();
          }
          CommandLine cmdLine = bindings.buildCommandLineObject(job, new File(workingDirTes), tesMapper);

          EnvironmentVariableRequirement env = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
          Map<String, String> variables = env == null ? new HashMap<>() : env.getVariables();
          
          List<String> commandLine = new ArrayList<String>();
          List<String> parts = cmdLine.getParts();
          
          if (parts.stream().anyMatch(p -> p.contains(" ") || p.contains("&"))) { //differently passes cmdline if escaping is needed, or if it isn't in parts
            commandLine.add("/bin/sh");
            commandLine.add("-c");
            commandLine.add(cmdLine.build());
          } else {
            commandLine.addAll(parts);
          }
          
          commands.add(new TESDockerExecutor(imageId, commandLine, workingDirTes, //noticed stdout/in files weren't mapped always properly, should be changed
              StringUtils.isEmpty(cmdLine.getStandardIn()) ? null
                  : cmdLine.getStandardIn().startsWith("/mnt/") ? cmdLine.getStandardIn() : inputsTes + cmdLine.getStandardIn(),
              StringUtils.isEmpty(cmdLine.getStandardOut()) ? null
                  : cmdLine.getStandardOut().startsWith(workingDirTes) ? cmdLine.getStandardOut() : workingDirTes + cmdLine.getStandardOut(),
              StringUtils.isEmpty(cmdLine.getStandardError()) ? null
                  : cmdLine.getStandardError().startsWith(workingDirTes) ? cmdLine.getStandardError() : workingDirTes + cmdLine.getStandardError(),
              variables));
        }

        Integer cpus = null;
        Double disk = null;
        Double ram = null;
        ResourceRequirement jobResourceRequirement = bindings.getResourceRequirement(job);
        if (jobResourceRequirement != null) {
          cpus = (jobResourceRequirement.getCpuMin() != null) ? jobResourceRequirement.getCpuMin().intValue() : null;
          disk = (jobResourceRequirement.getDiskSpaceMinMB() != null) ? jobResourceRequirement.getDiskSpaceMinMB().doubleValue() / 1000.0 : null;
          ram = (jobResourceRequirement.getMemMinMB() != null) ? jobResourceRequirement.getMemMinMB().doubleValue() / 1000.0 : null;
        }

        volumes.add(new TESVolume("working_dir", disk, null, workingDirTes, false));
        volumes.add(new TESVolume("inputs", disk, null, inputsTes, false));
        
        TESResources resources = new TESResources(cpus, false, ram, volumes, null);

        TESTask task = new TESTask(
          job.getName(),
          DEFAULT_PROJECT,
          job.getRootId().toString(),
          inputs,
          outputs,
          resources,
          job.getId().toString(),
          commands
        );
        
        TESJobId tesJobId = tesHttpClient.runTask(task);

        TESJob tesJob;
        do {
          Thread.sleep(1000L);
          tesJob = tesHttpClient.getJob(tesJobId);
          if (tesJob == null) {
            throw new TESServiceException("TESJob is not created. JobId = " + job.getId());
          }
        } while(!isFinished(tesJob));
        
        return tesJob;
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
  public void cancel(List<UUID> ids, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void freeResources(UUID rootId, Map<String, Object> config) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isRunning(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public Map<String, Object> getResult(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isStopped() {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public JobStatus findStatus(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public String getType() {
    return TYPE;
  }

}
