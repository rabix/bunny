package org.rabix.executor.container.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.executor.config.DockerConfigation;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.container.impl.TemplateScope.VolumeMap;
import org.rabix.executor.handler.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Docker based implementation of {@link ContainerHandler}
 */
public class UserDockerContainerHandler implements ContainerHandler {

  private static final Logger logger = LoggerFactory.getLogger(UserDockerContainerHandler.class);

  public static final String DIRECTORY_MAP_MODE = "rw";

  public static final String EXECUTOR_OVERRIDE_COMMAND = "executor.override.command";

  public static final String HOME_ENV_VAR = "HOME";
  public static final String TMPDIR_ENV_VAR = "TMPDIR";

  private final Job job;
  private final DockerContainerRequirement dockerResource;
  private final File workingDir;

  private Future<Integer> processFuture;
  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  private String commandLine;

  private final String dockerOverride;
  private String errorLog;


  public UserDockerContainerHandler(Job job, Configuration configuration, DockerContainerRequirement dockerResource, StorageConfiguration storageConfig,
      DockerConfigation dockerConfig, WorkerStatusCallback statusCallback) throws ContainerException {
    this.job = job;
    this.dockerResource = dockerResource;
    this.workingDir = storageConfig.getWorkingDir(job);
    this.dockerOverride = configuration.getString(EXECUTOR_OVERRIDE_COMMAND);
  }

  private class NoEscapeMustacheFactory extends DefaultMustacheFactory {
    @Override
    public void encode(String value, Writer writer) {
      try {
        writer.write(value);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void start() throws ContainerException {
    try {
      TemplateScope scope = new TemplateScope();
      scope.setJobId(job.getId());
      scope.setJobName(job.getName());
      String dockerPull = dockerResource.getDockerPull();
      if (StringUtils.isEmpty(dockerPull)) {
        dockerPull = "ubuntu";
      }
      scope.setImage(dockerPull);
      Map<String, String> volumes = new HashMap<>();
      FileValueHelper.getInputFiles(job).forEach(f -> {
        volumes.put(URI.create(f.getLocation()).getPath(), f.getPath());
        f.getSecondaryFiles().forEach(sec -> volumes.put(URI.create(sec.getLocation()).getPath(), sec.getPath()));
      });
      if (dockerResource.getDockerOutputDirectory() != null) {
        volumes.put(workingDir.getAbsolutePath(), dockerResource.getDockerOutputDirectory());
      } else {
        volumes.put(workingDir.getAbsolutePath(), workingDir.getAbsolutePath());
      }


      Bindings bindings = BindingsFactory.create(job);
      commandLine = bindings.buildCommandLineObject(job, workingDir, (String path, Map<String, Object> config) -> {
        return path;
      }).build();

      if (StringUtils.isEmpty(commandLine.trim())) {
        return;
      }

      scope.setVolumes(volumes.entrySet().stream().map(s -> new VolumeMap(s.getKey(), s.getValue())).collect(Collectors.toSet()));
      scope.setWorkingDir(workingDir.getAbsolutePath());

      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      EnvironmentVariableRequirement environmentVariableResource = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
      Map<String, String> environmentVariables = environmentVariableResource != null ? environmentVariableResource.getVariables()
          : new HashMap<String, String>();
      Resources resources = job.getResources();
      if (resources != null) {
        if (resources.getWorkingDir() != null) {
          environmentVariables.put(HOME_ENV_VAR, resources.getWorkingDir());
        }
        if (resources.getTmpDir() != null) {
          environmentVariables.put(TMPDIR_ENV_VAR, resources.getTmpDir());
        }
      }
      scope.setCommand(commandLine);
      if (!environmentVariables.isEmpty())
        scope.setEnv(environmentVariables);

      MustacheFactory mf = new NoEscapeMustacheFactory();
      Mustache mustache = mf.compile(new StringReader(dockerOverride), "dockerOverride");
      StringWriter sw = new StringWriter();
      mustache.execute(sw, scope);
      logger.info("RUNNING OVERRIDE:" + sw);
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("/bin/sh", "-c", sw.toString());

      processFuture = executorService.submit(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
          Process process = processBuilder.start();
          process.waitFor();
          List<String> lines = IOUtils.readLines(process.getErrorStream());
          if (lines != null && !lines.isEmpty()) {
            errorLog = lines.stream().reduce("", (a, b) -> a + "\n" + b);
          }
          return process.exitValue();
        }
      });
    } catch (Exception e) {
      logger.error("Failed to start container.", e);
      throw new ContainerException("Failed to start container.", e);
    }
  }

  private String normalizeCommandLine(String commandLine) {
    commandLine = commandLine.trim();
    if (commandLine.startsWith("\"") && commandLine.endsWith("\"")) {
      commandLine = commandLine.substring(1, commandLine.length() - 1);
    }
    if (commandLine.startsWith("'") && commandLine.endsWith("'")) {
      commandLine = commandLine.substring(1, commandLine.length() - 1);
    }
    return commandLine;
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
  public synchronized void stop() throws ContainerException {
    if (processFuture == null) {
      return;
    }
    processFuture.cancel(true);
  }

  @Override
  public synchronized boolean isStarted() throws ContainerException {
    return processFuture != null;
  }

  @Override
  public synchronized boolean isRunning() throws ContainerException {
    if (processFuture == null) {
      return false;
    }
    return !processFuture.isDone();
  }

  @Override
  public synchronized int getProcessExitStatus() throws ContainerException {
    try {
      return processFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ContainerException(e);
    }
  }

  @Override
  public void removeContainer() {

  }


  @Override
  public void dumpCommandLine() throws ContainerException {
    try {
      File commandLineFile = new File(workingDir, JobHandler.COMMAND_LOG);
      FileUtils.writeStringToFile(commandLineFile, commandLine);
    } catch (IOException e) {
      logger.error("Failed to dump command line into " + JobHandler.COMMAND_LOG);
      throw new ContainerException(e);
    }
  }

  @Override
  public String getProcessExitMessage() throws ContainerException {
    return errorLog;
  }
}
