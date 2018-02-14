package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.commons.lang3.StringUtils;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.tes.client.TESHTTPClientException;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.model.TESCreateTaskResponse;
import org.rabix.backend.tes.model.TESExecutor;
import org.rabix.backend.tes.model.TESFileType;
import org.rabix.backend.tes.model.TESGetTaskRequest;
import org.rabix.backend.tes.model.TESInput;
import org.rabix.backend.tes.model.TESOutput;
import org.rabix.backend.tes.model.TESResources;
import org.rabix.backend.tes.model.TESState;
import org.rabix.backend.tes.model.TESTask;
import org.rabix.backend.tes.model.TESView;
import org.rabix.backend.tes.service.TESServiceException;
import org.rabix.backend.tes.service.TESStorageException;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.FileValue.FileType;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
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
  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";

  @BindingAnnotation
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD})
  @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public static @interface TESWorker {
  }


  @Inject
  private TESHttpClient tesHttpClient;
  @Inject
  private TESStorageService storage;

  private Set<Future<TESWorkPair>> pendingResults = Collections.newSetFromMap(new ConcurrentHashMap<Future<TESWorkPair>, Boolean>());

  private ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);

  private EngineStub<?, ?, ?> engineStub;

  @Inject
  private Configuration configuration;
  @Inject
  private WorkerStatusCallback statusCallback;

  private void success(Job job, TESTask tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    try {
      Bindings bindings = BindingsFactory.create(job);
      Path dir = storage.localDir(job);
      if (!bindings.isSelfExecutable(job)) {
        TESOutput tesTaskParameter = tesJob.getOutputs().get(0);

        URI uri = URI.create(tesTaskParameter.getLocation() + "/");
        Path outDir = Paths.get(uri);
        dir = outDir;
      }
      job = bindings.postprocess(job, dir, HashAlgorithm.SHA1, (String path, Map<String, Object> config) -> path);
    } catch (Exception e) {
      logger.error("Couldn't process job", e);
      job = Job.cloneWithStatus(job, JobStatus.FAILED);
    }
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
        for (Iterator<Future<TESWorkPair>> iterator = pendingResults.iterator(); iterator.hasNext();) {
          Future<TESWorkPair> pending = (Future<TESWorkPair>) iterator.next();
          if (pending.isDone()) {
            try {
              TESWorkPair tesJob = pending.get();
              if (tesJob.tesTask.getState().equals(TESState.COMPLETE)) {
                success(tesJob.job, tesJob.tesTask);
              } else {
                fail(tesJob.job, tesJob.tesTask);
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
    }, 0, 300, TimeUnit.MILLISECONDS);
  }

  public void submit(Job job, UUID contextId) {
    pendingResults.add(taskPoolExecutor.submit(new TaskRunCallable(job)));
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


  public class TaskRunCallable implements Callable<TESWorkPair> {

    private Job job;
    private Path workDir;
    private Path localDir;

    public TaskRunCallable(Job job) {
      this.job = job;
      workDir = storage.workDir(job);
      localDir = storage.localDir(job);
    }

    @Override
    public TESWorkPair call() throws Exception {
      try {
        Bindings bindings = BindingsFactory.create(job);
        job = bindings.preprocess(job, localDir, (String path, Map<String, Object> config) -> path);
        List<Requirement> combinedRequirements = getRequirements(bindings);
        DockerContainerRequirement dockerContainerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
        if (dockerContainerRequirement != null && dockerContainerRequirement.getDockerOutputDirectory() != null) {
          localDir = Paths.get(dockerContainerRequirement.getDockerOutputDirectory());
          job = bindings.preprocess(job, localDir, (String path, Map<String, Object> config) -> path);
        }

        if (bindings.isSelfExecutable(job)) {
          return new TESWorkPair(job, new TESTask(null, TESState.COMPLETE, null, null, null, null, null, null, null, null, null, null));
        }

        Set<TESInput> inputs = new HashSet<>();
        Map<String, Object> wfInputs = job.getInputs();
        Collection<FileValue> flat = flatten(wfInputs);
        stageFileRequirements(combinedRequirements, workDir, flat);

        flat.forEach(fileValue -> {
          try {
            storage.stageFile(workDir, fileValue);
            inputs.add(new TESInput(fileValue.getName(), null, fileValue.getLocation(), fileValue.getPath(),
                fileValue.getType().equals(FileType.File) ? TESFileType.FILE : TESFileType.DIRECTORY, null));
          } catch (TESStorageException e) {
            e.printStackTrace();
          }
        });
        job = Job.cloneWithInputs(job, wfInputs);

        List<TESOutput> outputs = Collections.singletonList(
            new TESOutput(localDir.getFileName().toString(), null, workDir.toUri().toString(), localDir.toString(), TESFileType.DIRECTORY));

        CommandLine commandLine = bindings.buildCommandLineObject(job, localDir.toFile(), (String path, Map<String, Object> config) -> path);

        String commandLineToolStdout = commandLine.getStandardOut();
        if (commandLineToolStdout != null && !commandLineToolStdout.startsWith("/")) {
          commandLineToolStdout = localDir.resolve(commandLineToolStdout).toString();
        }

        String commandLineToolErrLog = commandLine.getStandardError();
        if (commandLineToolErrLog == null) {
          commandLineToolErrLog = DEFAULT_COMMAND_LINE_TOOL_ERR_LOG;
        }
        commandLineToolErrLog = localDir.resolve(commandLineToolErrLog).toString();

        List<TESExecutor> command = Collections.singletonList(new TESExecutor(getImageId(dockerContainerRequirement), buildCommandLine(commandLine),
            localDir.toString(), commandLine.getStandardIn(), commandLineToolStdout, commandLineToolErrLog, getVariables(combinedRequirements)));

        TESResources resources = getResources(combinedRequirements);
        TESTask task = new TESTask(job.getName(), null, new ArrayList<>(inputs), outputs, resources, command, null, null, null);

        TESCreateTaskResponse tesJobId = tesHttpClient.runTask(task);

        do {
          Thread.sleep(10000L);
          task = tesHttpClient.getTask(new TESGetTaskRequest(tesJobId.getId(), TESView.MINIMAL));
          if (task == null) {
            throw new TESServiceException("TESJob is not created. JobId = " + job.getId());
          }
        } while (!isFinished(task));
        task = tesHttpClient.getTask(new TESGetTaskRequest(tesJobId.getId(), TESView.FULL));
        return new TESWorkPair(job, task);
      } catch (TESHTTPClientException e) {
        logger.error("Failed to submit Job to TES", e);
        throw new TESServiceException("Failed to submit Job to TES", e);
      } catch (BindingException e) {
        logger.error("Failed to use Bindings", e);
        throw new TESServiceException("Failed to use Bindings", e);
      }
    }

    private List<String> buildCommandLine(CommandLine commandLine) {
      List<String> mainCommand = new ArrayList<>();
      List<String> parts = commandLine.getParts();
      StringBuilder joined = new StringBuilder();

      parts.forEach(part -> {
        if ((!mainCommand.isEmpty() && mainCommand.get(mainCommand.size() - 1).equals("-c")) || joined.length() > 0) {
          joined.append(" ").append(part);
        } else {
          mainCommand.add(part);
        }
      });
      if (joined.length() > 0) {
        mainCommand.add(joined.toString().trim());
      } else {
        joined.append(StringUtils.join(mainCommand, " "));
        mainCommand.clear();
        mainCommand.add(joined.toString());
        mainCommand.add(0, "-c");
        mainCommand.add(0, "/bin/sh");
      }
      return mainCommand;
    }

    private Map<String, String> getVariables(List<Requirement> combinedRequirements) {
      EnvironmentVariableRequirement envs = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
      Map<String, String> variables = new HashMap<>();
      if (envs != null) {
        variables = envs.getVariables();
      }
      variables.put("HOME", localDir.toString());
      variables.put("TMPDIR", localDir.toString());
      return variables;
    }

    private String getImageId(DockerContainerRequirement dockerContainerRequirement) {
      String imageId;
      if (dockerContainerRequirement == null) {
        imageId = "debian:stretch-slim";
      } else {
        imageId = dockerContainerRequirement.getDockerPull();
      }
      return imageId;
    }

    private void stageFileRequirements(List<Requirement> requirements, Path workDir, Collection<FileValue> old) throws TESStorageException {
      FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
      if (fileRequirementResource == null) {
        return;
      }

      List<SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
      if (fileRequirements == null) {
        return;
      }

      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);
        String filename = fileRequirement.getFilename();
        Path destinationFile = workDir.resolve(filename);
        if (fileRequirement instanceof SingleTextFileRequirement) {
          try {
            byte[] bytes = ((SingleTextFileRequirement) fileRequirement).getContent().getBytes();
            Files.createDirectories(destinationFile.getParent());
            Files.write(destinationFile, bytes);
          } catch (IOException e) {
            throw new TESStorageException(e.getMessage());
          }
          old.add(new FileValue(0l, localDir.resolve(filename).toString(), destinationFile.toUri().toString(), null, Collections.emptyList(), null, null));
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement) {
          FileValue content = ((SingleInputFileRequirement) fileRequirement).getContent();
          for (FileValue f : old) {
            if (f.getPath().equals(content.getPath())) {
              content = f;
            }
          }
          if (!filename.equals(content.getName())) {
            recursiveSet(content, filename);
          }
          if (content.getPath() == null) {
            content.setPath(storage.localDir(job).resolve(content.getPath()).toString());
          }
          if (!old.contains(content)) {
            old.add(content);
          }
        }
      }
    }

    private List<Requirement> getRequirements(Bindings bindings) throws BindingException {
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getRequirements(job));
      combinedRequirements.addAll(bindings.getHints(job));
      return combinedRequirements;
    }

    private void recursiveSet(FileValue file, String a) {
      file.setName(a);
      file.setPath(storage.localDir(job).resolve(a).toString());
      file.getSecondaryFiles().forEach(f -> {
        recursiveSet(f, f.getName());
      });
    }

    private Collection<FileValue> flatten(Map<String, Object> inputs) {
      List<FileValue> flat = new ArrayList<>();
      flatten(flat, inputs);
      return flat;
    }

    @SuppressWarnings({"rawtypes"})
    private void flatten(Collection<FileValue> inputs, Object value) {
      if (value instanceof Map)
        flatten(inputs, (Map) value);
      if (value instanceof List)
        flatten(inputs, (List) value);
      if (value instanceof FileValue)
        flatten(inputs, (FileValue) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void flatten(Collection<FileValue> inputs, Map value) {
      value.values().forEach(v -> flatten(inputs, v));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void flatten(Collection<FileValue> inputs, List value) {
      value.forEach(v -> flatten(inputs, v));
    }

    private void flatten(Collection<FileValue> inputs, FileValue value) {
      value.getSecondaryFiles().forEach(f -> flatten(inputs, f));
      if (value instanceof DirectoryValue) {
        List<FileValue> listing = ((DirectoryValue) value).getListing();
        if (!listing.isEmpty()) {
          listing.forEach(f -> flatten(inputs, f));
        } else {
          inputs.add(value);
        }
      } else {
        inputs.add(value);
      }
    }

    private TESResources getResources(List<Requirement> combinedRequirements) {
      Integer cpus = null;
      Double disk = null;
      Double ram = null;
      ResourceRequirement jobResourceRequirement = getRequirement(combinedRequirements, ResourceRequirement.class);
      if (jobResourceRequirement != null) {
        cpus = (jobResourceRequirement.getCpuMin() != null) ? jobResourceRequirement.getCpuMin().intValue() : null;
        disk = (jobResourceRequirement.getDiskSpaceMinMB() != null) ? jobResourceRequirement.getDiskSpaceMinMB().doubleValue() / 1000.0 : null;
        ram = (jobResourceRequirement.getMemMinMB() != null) ? jobResourceRequirement.getMemMinMB().doubleValue() / 1000.0 : null;
      }
      TESResources resources = new TESResources(cpus, false, ram, disk, null);
      return resources;
    }

    private boolean isFinished(TESTask tesJob) {
      return tesJob.getState().equals(TESState.CANCELED) || tesJob.getState().equals(TESState.COMPLETE) || tesJob.getState().equals(TESState.EXECUTOR_ERROR)
          || tesJob.getState().equals(TESState.SYSTEM_ERROR);
    }
  }

  private class TESWorkPair {
    Job job;
    TESTask tesTask;

    public TESWorkPair(Job job, TESTask tesTask) {
      super();
      this.job = job;
      this.tesTask = tesTask;
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
