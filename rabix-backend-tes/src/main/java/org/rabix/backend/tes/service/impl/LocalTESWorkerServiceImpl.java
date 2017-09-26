package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import java.util.stream.Collectors;

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
import org.rabix.backend.tes.model.TESDockerExecutor;
import org.rabix.backend.tes.model.TESJobId;
import org.rabix.backend.tes.model.TESResources;
import org.rabix.backend.tes.model.TESState;
import org.rabix.backend.tes.model.TESTask;
import org.rabix.backend.tes.model.TESTaskParameter;
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

  @BindingAnnotation
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD})
  @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public static @interface TESWorker {
  }

  public final static String PYTHON_DEFAULT_DOCKER_IMAGE = "frolvlad/alpine-python2";
  public final static String DEFAULT_PROJECT = "default";
  public final static String DEFAULT_COMMAND_LINE_TOOL_ERR_LOG = "job.err.log";

  @Inject
  private TESHttpClient tesHttpClient;
  @Inject
  private TESStorageService storage;

  private Set<Future<TESCallbackObject>> pendingResults = Collections.newSetFromMap(new ConcurrentHashMap<Future<TESCallbackObject>, Boolean>());

  private ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private java.util.concurrent.ExecutorService taskPoolExecutor = Executors.newFixedThreadPool(10);

  private EngineStub<?, ?, ?> engineStub;

  @Inject
  private Configuration configuration;
  @Inject
  private WorkerStatusCallback statusCallback;


  public LocalTESWorkerServiceImpl() {}


  private void success(Job job, TESTask tesJob) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    try {
      Bindings bindings = BindingsFactory.create(job);
      Path localDir = storage.localDir(job);
      if (!bindings.isSelfExecutable(job)) {
        TESTaskParameter tesTaskParameter = tesJob.getOutputs().get(0);
        try {
          Path outDir = Paths.get(URI.create(tesTaskParameter.getLocation() + "/"));
          downloadDirectory(localDir, outDir);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      job = bindings.postprocess(job, localDir, HashAlgorithm.SHA1, (String path, Map<String, Object> config) -> path);
    } catch (BindingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      job = statusCallback.onJobCompleted(job);
    } catch (WorkerStatusCallbackException e1) {
      logger.warn("Failed to execute statusCallback: {}", e1);
    }
    engineStub.send(job);
  }


  private void downloadDirectory(Path localDir, Path outDir) throws IOException {
    Files.createDirectories(localDir);
    Files.list(outDir).forEach(p -> {
      try {
        Path resolved = localDir.resolve(outDir.relativize(p.toAbsolutePath()).toString());
        if (!Files.isDirectory(p)) {
          Files.copy(p.toAbsolutePath(), resolved, StandardCopyOption.REPLACE_EXISTING);
        } else {
          downloadDirectory(resolved, p.toAbsolutePath());
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
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
        for (Iterator<Future<TESCallbackObject>> iterator = pendingResults.iterator(); iterator.hasNext();) {
          Future<TESCallbackObject> pending = (Future<TESCallbackObject>) iterator.next();
          if (pending.isDone()) {
            try {
              TESCallbackObject tesJob = pending.get();
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
    }, 0, 1, TimeUnit.SECONDS);
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


  public class TaskRunCallable implements Callable<TESCallbackObject> {

    private Job job;

    public TaskRunCallable(Job job) {
      this.job = job;
    }

    @Override
    public TESCallbackObject call() throws Exception {
      try {
        Bindings bindings = BindingsFactory.create(job);
        Path workDir = storage.workDir(job);
        Path localDir = storage.localDir(job);


        List<Requirement> combinedRequirements = getRequirements(bindings);
        DockerContainerRequirement dockerContainerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);

        if (dockerContainerRequirement != null && dockerContainerRequirement.getDockerOutputDirectory() != null) {
          localDir = Paths.get(dockerContainerRequirement.getDockerOutputDirectory());
        }

        job = bindings.preprocess(job, localDir, (String path, Map<String, Object> config) -> path);
        List<TESTaskParameter> inputs = new ArrayList<>();
        List<TESTaskParameter> outputs = new ArrayList<>();
        List<String> mainCommand = new ArrayList<>();
        List<TESDockerExecutor> commands = new ArrayList<>();

        // Prepare CWL input file into TES-compatible files
        storage.transformInputFiles(job);

        addInput(inputs, job.getInputs());

        outputs.add(new TESTaskParameter(localDir.getFileName().toString(), null, workDir.toUri().toString(), localDir.toString(), FileType.Directory, false));

        combinedRequirements = getRequirements(bindings);
        Map<String, Path> staged = stageFileRequirements(combinedRequirements, workDir);
        if (staged != null) {
          Map<String, TESTaskParameter> collected = staged.entrySet().stream()
              .collect(Collectors.toMap(e -> e.getKey(), e -> getInput(workDir.relativize(e.getValue()).toString(), e.getValue())));
          try {
            job = FileValueHelper.updateInputFiles(job, fileValue -> {
              if (collected.containsKey(fileValue.getPath())) {
                fileValue.setPath(collected.get(fileValue.getPath()).getPath());
              }
              return fileValue;
            });
          } catch (BindingException e) {
          }
          inputs.addAll(collected.values());
        }


        if (bindings.isSelfExecutable(job)) {
          return new TESCallbackObject(job, new TESTask(null, null, null, null, null, null, null, null, null, TESState.COMPLETE, null));
        }

        CommandLine commandLine = bindings.buildCommandLineObject(job, localDir.toFile(), (String path, Map<String, Object> config) -> path);

        List<String> parts = commandLine.getParts();
        StringBuilder joined = new StringBuilder();


        parts.stream().forEach(part -> {
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

        String commandLineToolStdout = commandLine.getStandardOut();
        if (commandLineToolStdout != null && !commandLineToolStdout.startsWith("/")) {
          commandLineToolStdout = localDir.resolve(commandLineToolStdout).toString();
        }

        String commandLineToolErrLog = commandLine.getStandardError();
        if (commandLineToolErrLog == null) {
          commandLineToolErrLog = DEFAULT_COMMAND_LINE_TOOL_ERR_LOG;
        }
        commandLineToolErrLog = localDir.resolve(commandLineToolErrLog).toString();

        EnvironmentVariableRequirement envs = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
        Map<String, String> variables = new HashMap<>();
        if (envs != null) {
          variables = envs.getVariables();
        }
        variables.put("HOME", localDir.toString());
        variables.put("TMPDIR", localDir.toString());

        String imageId;
        if (dockerContainerRequirement == null) {
          imageId = "ubuntu";
        } else {
          imageId = dockerContainerRequirement.getDockerPull();
        }

        commands.add(new TESDockerExecutor(imageId, mainCommand, localDir.toString(), commandLine.getStandardIn(), commandLineToolStdout, commandLineToolErrLog,
            variables));

        TESResources resources = getResources(combinedRequirements);

        TESTask task = new TESTask(job.getName(), DEFAULT_PROJECT, job.getRootId().toString(), inputs, outputs, resources, job.getId().toString(), commands);

        TESJobId tesJobId = tesHttpClient.runTask(task);

        do {
          Thread.sleep(100L);
          task = tesHttpClient.getTask(tesJobId);
          if (task == null) {
            throw new TESServiceException("TESJob is not created. JobId = " + job.getId());
          }
        } while (!isFinished(task));
        return new TESCallbackObject(job, task);
      } catch (TESHTTPClientException e) {
        logger.error("Failed to submit Job to TES", e);
        throw new TESServiceException("Failed to submit Job to TES", e);
      } catch (BindingException e) {
        logger.error("Failed to use Bindings", e);
        throw new TESServiceException("Failed to use Bindings", e);
      }
    }

    private Map<String, Path> stageFileRequirements(List<Requirement> requirements, Path workDir) throws IOException {
      FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
      if (fileRequirementResource == null) {
        return null;
      }

      List<SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
      if (fileRequirements == null) {
        return null;
      }

      Map<String, Path> stagedFiles = new HashMap<>();

      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);

        Path destinationFile = workDir.resolve(fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          Files.write(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent().getBytes());
          stagedFiles.put(fileRequirement.getFilename(), destinationFile);
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement) {
          FileValue content = ((SingleInputFileRequirement) fileRequirement).getContent();
          content.setName(fileRequirement.getFilename());
          if (content instanceof DirectoryValue) {
            stage(content, workDir, workDir, stagedFiles);
            continue;
          }
          String path = content.getPath();
          stagedFiles.put(path, destinationFile);
          Path file = Paths.get(path);
          if (!Files.exists(file)) {
            continue;
          }
          Files.copy(file, destinationFile);
        }
      }
      return stagedFiles;
    }

    private List<Requirement> getRequirements(Bindings bindings) throws BindingException {
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));
      return combinedRequirements;
    }

    @SuppressWarnings({"rawtypes"})
    private void addInput(List<TESTaskParameter> inputs, Object value) {
      if (value instanceof Map)
        addInput(inputs, (Map) value);
      if (value instanceof List)
        addInput(inputs, (List) value);
      if (value instanceof FileValue)
        addInput(inputs, (FileValue) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addInput(List<TESTaskParameter> inputs, Map value) {
      value.values().forEach(v -> addInput(inputs, v));
    }

    private TESTaskParameter getInput(String name, Path path) {
      return new TESTaskParameter(name, null, path.toUri().toString(), storage.localDir(job).resolve(name).toString(),
          Files.isDirectory(path) ? FileType.Directory : FileType.File, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addInput(List<TESTaskParameter> inputs, List value) {
      value.forEach(v -> addInput(inputs, v));
    }

    private void addInput(List<TESTaskParameter> inputs, FileValue fileValue) {
      fileValue.getSecondaryFiles().forEach(f -> addInput(inputs, f));
      if (fileValue instanceof DirectoryValue) {
        List<FileValue> listing = ((DirectoryValue) fileValue).getListing();
        if (!listing.isEmpty()) {
          listing.stream().forEach(f -> addInput(inputs, f));
        } else {
          inputs.add(new TESTaskParameter(fileValue.getName(), null, fileValue.getLocation(), fileValue.getPath(), fileValue.getType(), false));
        }
      } else {
        inputs.add(new TESTaskParameter(fileValue.getName(), null, fileValue.getLocation(), fileValue.getPath(), fileValue.getType(), false));
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
      return tesJob.getState().equals(TESState.CANCELED) || tesJob.getState().equals(TESState.COMPLETE) || tesJob.getState().equals(TESState.ERROR)
          || tesJob.getState().equals(TESState.SYSTEM_ERROR);
    }
  }
  private class TESCallbackObject {
    Job job;
    TESTask tesTask;

    public TESCallbackObject(Job job, TESTask tesTask) {
      super();
      this.job = job;
      this.tesTask = tesTask;
    }
  }

  private static void stage(FileValue file, Path parentDir, Path workDir, Map<String, Path> stagedFiles) {
    String path = file.getPath();
    if (path == null)
      path = file.getName();
    Path filePath = Paths.get(path);
    Path destination = parentDir.resolve(file.getName());

    List<FileValue> secondaryFiles = file.getSecondaryFiles();
    if (secondaryFiles != null) {
      secondaryFiles.stream().forEach(f -> stage(f, parentDir, workDir, stagedFiles));
    }
    if (file instanceof DirectoryValue) {
      List<FileValue> listing = ((DirectoryValue) file).getListing();
      if (listing.isEmpty()) {
        stagedFiles.put(workDir.relativize(destination).toString(), destination.resolve("./").normalize());
        try {
          Files.createDirectories(destination);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else
        listing.stream().forEach(f -> stage(f, destination, workDir, stagedFiles));
    } else {
      try {
        stagedFiles.put(workDir.relativize(destination).toString(), destination);
        Files.copy(filePath, destination);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
