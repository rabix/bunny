package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
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
import org.rabix.bindings.model.DirectoryValue;
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

import com.google.inject.Inject;

public class LocalTESWorkerServiceImpl implements WorkerService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESWorkerServiceImpl.class);

  public final static String PYTHON_DEFAULT_DOCKER_IMAGE = "frolvlad/alpine-python2";
  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix-tes-cli";
//  public final static String BUNNY_COMMAND_LINE_DOCKER_IMAGE = "rabix/tes-command-line:v2";
  public final static String DEFAULT_PROJECT = "default";
  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";

  private TESHttpClient tesHttpClient;
  private TESStorageService storage;

  private final Set<PendingResult> pendingResults = new ConcurrentHashSet<>();
  
  private final ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private final java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);
  
  private EngineStub<?, ?, ?> engineStub;
  
  private final Configuration configuration;
  private final WorkerStatusCallback statusCallback;
  
  private class PendingResult {
    private Job job;
    private Future<TESJob> future;
    
    public PendingResult(Job job, Future<TESJob> future) {
      this.job = job;
      this.future = future;
    }
  }
  
  @Inject
  public LocalTESWorkerServiceImpl(final TESHttpClient tesHttpClient, final TESStorageService storage, final WorkerStatusCallback statusCallback, final Configuration configuration) {
    this.tesHttpClient = tesHttpClient;
    this.storage = storage;
    this.configuration = configuration;
    this.statusCallback = statusCallback;
    
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
  
  private void success(Job job, TESJob tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    Map<String, Object> result = null;
    try {
      result = (Map<String, Object>) FileValue.deserialize(
        JSONHelper.readMap(
          tesJob.getLogs().get(tesJob.getLogs().size() - 1).getStdout()
        )
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to process output files: {}", e);
    }

    try {
      result = storage.transformOutputFiles(result, job.getRootId().toString(), job.getName());
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

  public void start(Job job, UUID contextId) {
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
    
    @Override
    public TESJob call() throws Exception {
      try {
        Bindings bindings = BindingsFactory.create(job);
        job = bindings.preprocess(job, storage.stagingPath(job.getRootId().toString(), job.getName()).toFile(), null);

        List<TESTaskParameter> inputs = new ArrayList<>();
        List<TESTaskParameter> outputs = new ArrayList<>();
        List<TESVolume> volumes = new ArrayList<>();
        List<String> initCommand = new ArrayList<>();
        List<String> mainCommand = new ArrayList<>();
        List<String> finalizeCommand = new ArrayList<>();
        List<TESDockerExecutor> commands = new ArrayList<>();

        // TODO this has the effect of ensuring the working directory is created
        //      but the interface isn't great. Need to think about a better interface.
        storage.stagingPath(job.getRootId().toString(), job.getName(), "working_dir", "TODO");
        storage.stagingPath(job.getRootId().toString(), job.getName(), "inputs", "TODO");

        // Prepare CWL input file into TES-compatible files
        job = storage.transformInputFiles(job);

        // Add all job inputs to TES Job inputs parameters
        FileValueHelper.updateInputFiles(job, (FileValue fileValue) -> {
          inputs.add(new TESTaskParameter(
            fileValue.getName(),
            null,
            fileValue.getLocation(),
            fileValue.getPath(),
            (fileValue instanceof DirectoryValue) ? FileType.Directory.name() : FileType.File.name(),
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
                      (f instanceof DirectoryValue) ? FileType.Directory.name() : FileType.File.name(),
                      false
              ));
            }
          }
          return fileValue;
        });

        // Write job.json file
        FileUtils.writeStringToFile(
          storage.stagingPath(job.getRootId().toString(), job.getName(), "inputs", "job.json").toFile(),
          JSONHelper.writeObject(job)
        );

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
          storage.stagingPath(job.getRootId().toString(), job.getName(), "inputs", "job.json").toUri().toString(),
          storage.containerPath("inputs", "job.json").toString(),
          FileType.File.name(),
          false
        ));

        // TODO should explicitly name all output files, instead of entire directory?
        //      but how to handle glob?
        outputs.add(new TESTaskParameter(
          "working_dir",
          null,
          storage.outputPath(job.getRootId().toString(), job.getName(), "working_dir").toUri().toString(),
          storage.containerPath("working_dir").toString(),
          FileType.Directory.name(),
          false
        ));

        //TODO why are these outputs?
        /*if (!bindings.isSelfExecutable(job)) {
          outputs.add(new TESTaskParameter(
            "command.sh",
            null,
            storage.outputPath(job.getId(), "command.sh").toUri().toString(),
            storage.containerPath("command.sh").toString(),
            FileType.File.name(),
            false
          ));
          outputs.add(new TESTaskParameter(
            "environment.sh",
            null,
            storage.outputPath(job.getId(), "environment.sh").toUri().toString(),
            storage.containerPath("environment.sh").toString(),
            FileType.File.name(),
            false
          ));
         }*/

        // Initialization command
        initCommand.add("/usr/share/rabix-tes-command-line/rabix");
        initCommand.add("-j");
        initCommand.add(storage.containerPath("inputs", "job.json").toString());
        initCommand.add("-w");
        initCommand.add(storage.containerPath("working_dir").toString());
        initCommand.add("-m");
        initCommand.add("initialize");

        commands.add(new TESDockerExecutor(
          BUNNY_COMMAND_LINE_DOCKER_IMAGE,
          initCommand,
          storage.containerPath("working_dir").toString(),
          null,
          storage.containerPath("working_dir", "standard_out.log").toString(),
          storage.containerPath("working_dir", "standard_error.log").toString()
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
            storage.containerPath("working_dir").toFile(),
            (String path, Map<String, Object> config) -> path
          );

          String commandLineToolStdout = commandLine.getStandardOut();
          if (commandLineToolStdout != null && !commandLineToolStdout.startsWith("/")) {
              commandLineToolStdout = storage.containerPath("working_dir", commandLineToolStdout).toString();
          }

          String commandLineToolErrLog = commandLine.getStandardError();
          if (commandLineToolErrLog == null) {
            commandLineToolErrLog = storage.containerPath("working_dir", DEFAULT_COMMAND_LINE_TOOL_ERR_LOG).toString();
          }

          // Main job command
          commands.add(new TESDockerExecutor(
            imageId,
            mainCommand,
            storage.containerPath("working_dir").toString(),
            null,
            commandLineToolStdout,
            commandLineToolErrLog
          ));
        }

        // Finalization command

        finalizeCommand.add("/usr/share/rabix-tes-command-line/rabix");
        finalizeCommand.add("-j");
        finalizeCommand.add(storage.containerPath("inputs", "job.json").toString());
        finalizeCommand.add("-w");
        finalizeCommand.add(storage.containerPath("working_dir").toString());
        finalizeCommand.add("-m");
        finalizeCommand.add("finalize");

        commands.add(new TESDockerExecutor(
          BUNNY_COMMAND_LINE_DOCKER_IMAGE,
          finalizeCommand,
          storage.containerPath("working_dir").toString(),
          null,
          // TODO maybe move these to a "rabix" output path so there's zero chance of conflict with the tool
          storage.containerPath("working_dir", "standard_out.log").toString(),
          storage.containerPath("working_dir", "standard_error.log").toString()
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
          "working_dir",
          disk,
          null,
          storage.containerPath("working_dir").toString(),
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

        TESJob tesJob;
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
  public void stop(List<UUID> ids, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void free(UUID rootId, Map<String, Object> config) {
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

}
