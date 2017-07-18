package org.rabix.backend.tes.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.tes.client.TESHTTPClientException;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.model.TESDockerExecutor;
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
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.FileValue.FileType;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.helper.JSONHelper;
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
//  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix-tes-cli";
  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix/tes-command-line:v3";
  public final static String DEFAULT_PROJECT = "default";
  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";

  @Inject
  private TESHttpClient tesHttpClient;
  @Inject
  private TESStorageService storage;

  private Set<PendingResult> pendingResults = Collections.newSetFromMap(new ConcurrentHashMap<PendingResult, Boolean>());
  
  private ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);
  
  private EngineStub<?, ?, ?> engineStub;
  
  @Inject
  private Configuration configuration;
  @Inject
  private WorkerStatusCallback statusCallback;

  static String WORKING_DIR = "working_dir";
  
  private class PendingResult {
    private Job job;
    private Future<TESTask> future;
    
    public PendingResult(Job job, Future<TESTask> future) {
      this.job = job;
      this.future = future;
    }
  }
  
  public LocalTESWorkerServiceImpl() {
  }
  
  @SuppressWarnings("unchecked")
  private void success(Job job, TESTask tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    Map<String, Object> result = null;
    try {
      result = (Map<String, Object>) FileValue.deserialize(
        JSONHelper.readMap(
          tesJob.getLogs().get(0).getLogs().get(tesJob.getLogs().get(0).getLogs().size() - 1).getStdout()
        )
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to process output files: {}", e);
    }

    try {
      result = storage.transformOutputFiles(result, job);
    } catch (BindingException e) {
      logger.error("Failed to process output files", e);
      throw new RuntimeException("Failed to process output files", e);
    }

    job = Job.cloneWithOutputs(job, result);
    job = Job.cloneWithMessage(job, "Success");
    try {
      job = statusCallback.onJobCompleted(job);
    } catch (WorkerStatusCallbackException e1) {
      logger.warn("Failed to execute statusCallback: {}", e1);
    }
    engineStub.send(job);
  }
  
  private void fail(Job job, TESTask tesJob) {
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
              TESTask tesJob = pending.future.get();
              if (tesJob.getState().equals(TESState.COMPLETE)) {
                success(pending.job, tesJob);
              } else {
                fail(pending.job, tesJob);
              }
              iterator.remove();
            } catch (InterruptedException | ExecutionException e) {
              logger.error("Failed to retrieve TESTask", e);
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

  
  public class TaskRunCallable implements Callable<TESTask> {

    private Job job;
    
    public TaskRunCallable(Job job) {
      this.job = job;
    }
    
    @Override
    public TESTask call() throws Exception {
      try {
        Bindings bindings = BindingsFactory.create(job);
        job = bindings.preprocess(job, new File(storage.workDir(job).toString()), (String path, Map<String, Object> config) -> path);

        List<TESTaskParameter> inputs = new ArrayList<>();
        List<TESTaskParameter> outputs = new ArrayList<>();
        List<TESVolume> volumes = new ArrayList<>();
        List<String> initCommand = new ArrayList<>();
        List<String> mainCommand = new ArrayList<>();
        List<String> finalizeCommand = new ArrayList<>();
        List<TESDockerExecutor> commands = new ArrayList<>();

        // Prepare CWL input file into TES-compatible files
        job = storage.transformInputFiles(job);

        // Add all job inputs to TES Job inputs parameters
        FileValueHelper.updateInputFiles(job, (FileValue fileValue) -> {
          inputs.add(new TESTaskParameter(
            fileValue.getName(),
            null,
            fileValue.getLocation(),
            fileValue.getPath(),
            fileValue.getType(),
            false
          ));
          List<FileValue> secondaryFiles = fileValue.getSecondaryFiles();
          if (secondaryFiles != null) {
            for (FileValue f : secondaryFiles) {
              inputs.add(new TESTaskParameter(
                      f.getName(),
                      null,
                      f.getLocation(),
                      f.getPath(),
                      f.getType(),
                      false
              ));
            }
          }
          return fileValue;
        });
        

      /*
        inputs.add(new TESTaskParameter(
          "inputs",
          null,
          storage.stagingPath(job.getId(), "inputs").toUri().toString(),
          storage.containerPath("inputs").toString(),
          FileType.Directory.name(),
          false));
          */

        inputs.add(new TESTaskParameter(
          "job.json",
          null,
          storage.writeJobFile(job).toUri().toString(),
          storage.containerPath("inputs", "job.json").toString(),
          FileType.File,
          false
        ));

        outputs.add(new TESTaskParameter(
          WORKING_DIR,
          null,
          storage.workDir(job).toUri().toString(),
          storage.containerPath(WORKING_DIR).toString(),
          FileType.Directory,
          false
        ));

        // Initialization command
        initCommand.add("/usr/share/rabix-tes-command-line/rabix");
        initCommand.add("-j");
        initCommand.add(storage.containerPath("inputs", "job.json").toString());
        initCommand.add("-w");
        initCommand.add(storage.containerPath(WORKING_DIR).toString());
        initCommand.add("-m");
        initCommand.add("initialize");

        commands.add(new TESDockerExecutor(
          BUNNY_COMMAND_LINE_DOCKER_IMAGE,
          initCommand,
          storage.containerPath(WORKING_DIR).toString(),
          null,
          storage.containerPath(WORKING_DIR, "standard_out.log").toString(),
          storage.containerPath(WORKING_DIR, "standard_error.log").toString(), null
        ));
        
        List<Requirement> combinedRequirements = new ArrayList<>();
        combinedRequirements.addAll(bindings.getHints(job));
        combinedRequirements.addAll(bindings.getRequirements(job));
        DockerContainerRequirement dockerContainerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
        String imageId;
        if (dockerContainerRequirement == null) {
          imageId = PYTHON_DEFAULT_DOCKER_IMAGE;
        } else {
          imageId = dockerContainerRequirement.getDockerPull();
        }
        
        if (!bindings.isSelfExecutable(job)) {
          mainCommand.add("/bin/sh");
          mainCommand.add("command.sh");

          CommandLine commandLine = bindings.buildCommandLineObject(
            job,
            // TODO needs local staging directory?
            //      is "File" the wrong type for this argument? Why is it a container path?
            storage.containerPath(WORKING_DIR).toFile(),
            (String path, Map<String, Object> config) -> path
          );

          String commandLineToolStdout = commandLine.getStandardOut();
          if (commandLineToolStdout != null && !commandLineToolStdout.startsWith("/")) {
              commandLineToolStdout = storage.containerPath(WORKING_DIR, commandLineToolStdout).toString();
          }

          String commandLineToolErrLog = commandLine.getStandardError();
          if (commandLineToolErrLog == null) {
            commandLineToolErrLog = storage.containerPath(WORKING_DIR, DEFAULT_COMMAND_LINE_TOOL_ERR_LOG).toString();
          }

          // Main job command
          commands.add(new TESDockerExecutor(
            imageId,
            mainCommand,
            storage.containerPath(WORKING_DIR).toString(),
            null,
            commandLineToolStdout,
            commandLineToolErrLog, null
          ));
        }

        // Finalization command

        finalizeCommand.add("/usr/share/rabix-tes-command-line/rabix");
        finalizeCommand.add("-j");
        finalizeCommand.add(storage.containerPath("inputs", "job.json").toString());
        finalizeCommand.add("-w");
        finalizeCommand.add(storage.containerPath(WORKING_DIR).toString());
        finalizeCommand.add("-m");
        finalizeCommand.add("finalize");

        commands.add(new TESDockerExecutor(
          BUNNY_COMMAND_LINE_DOCKER_IMAGE,
          finalizeCommand,
          storage.containerPath(WORKING_DIR).toString(),
          null,
          // TODO maybe move these to a "rabix" output path so there's zero chance of conflict with the tool
          storage.containerPath(WORKING_DIR, "standard_out.log").toString(),
          storage.containerPath(WORKING_DIR, "standard_error.log").toString(), null
        ));

        Integer cpus = null;
        Double disk = null;
        Double ram = null;
        ResourceRequirement jobResourceRequirement = bindings.getResourceRequirement(job);
        if (jobResourceRequirement != null) {
          cpus = (jobResourceRequirement.getCpuMin() != null) ? jobResourceRequirement.getCpuMin().intValue() : null;
          disk = (jobResourceRequirement.getDiskSpaceMinMB() != null) ? jobResourceRequirement.getDiskSpaceMinMB().doubleValue() / 1000.0 : null;
          ram = (jobResourceRequirement.getMemMinMB() != null) ? jobResourceRequirement.getMemMinMB().doubleValue() / 1000.0 : null;
        }

        volumes.add(new TESVolume(
          WORKING_DIR,
          disk,
          null,
          storage.containerPath(WORKING_DIR).toString(),
          false
        ));

        volumes.add(new TESVolume(
          "inputs",
          disk,
          null,
          storage.containerPath("inputs").toString(),
          true
        ));

        TESResources resources = new TESResources(
          cpus,
          false,
          ram,
          volumes,
          null
        );

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

        TESTask tesJob;
        do {
          Thread.sleep(1000L);
          tesJob = tesHttpClient.getTask(tesJobId);
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
    
    private boolean isFinished(TESTask tesJob) {
      return tesJob.getState().equals(TESState.CANCELED) || 
          tesJob.getState().equals(TESState.COMPLETE) || 
          tesJob.getState().equals(TESState.ERROR) || 
          tesJob.getState().equals(TESState.SYSTEMERROR);
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
