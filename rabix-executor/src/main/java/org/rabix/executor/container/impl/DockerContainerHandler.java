package org.rabix.executor.container.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.common.retry.Retry;
import org.rabix.executor.config.StorageConfig;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.ExecutorStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;

/**
 * Docker based implementation of {@link ContainerHandler}
 */
public class DockerContainerHandler implements ContainerHandler {

  private static final Logger logger = LoggerFactory.getLogger(DockerContainerHandler.class);
  
  private static final String dockerHubServer = "https://index.docker.io/v1/";

  public static final String DIRECTORY_MAP_MODE = "rw";
  public static final String COMMAND_FILE = "cmd.log";
  
  private String containerId;
  private DockerClientLockDecorator dockerClient;

  private final Job job;
  private final DockerContainerRequirement dockerResource;

  private final File workingDir;
  private final Configuration configuration;

  private boolean isConfigAuthEnabled;

  private Integer overrideResultStatus = null;

  private ExecutorStatusCallback statusCallback;

  public DockerContainerHandler(Job job, DockerContainerRequirement dockerResource, Configuration configuration, ExecutorStatusCallback statusCallback, DockerClientLockDecorator dockerClient) throws ContainerException {
    this.job = job;
    this.dockerClient = dockerClient;
    this.dockerResource = dockerResource;
    this.configuration = configuration;
    this.statusCallback = statusCallback;
    this.workingDir = StorageConfig.getWorkingDir(job, configuration);
    this.isConfigAuthEnabled = configuration.getBoolean("docker.override.auth.enabled", false);
  }

  private void pull(String image) throws ContainerException {
    logger.debug("Pulling docker image");
    VerboseLogger.log(String.format("Pulling docker image %s", image));

    try {
      statusCallback.onContainerImagePullStarted(image);
      if (isConfigAuthEnabled) {
        dockerClient.pull(image);
      } else {
        try {
          String serverAddress = extractServerName(image);
          AuthConfig authConfig = AuthConfig.fromDockerConfig(serverAddress).build();
          this.dockerClient.pull(image, authConfig);
        } catch (IOException e) {
          logger.debug("Can't find docker config file", e);
          dockerClient.pull(image);
        }
      }
      statusCallback.onContainerImagePullCompleted(image);
    } catch (DockerException | InterruptedException e) {
      logger.error("Failed to pull " + image, e);
      throw new ContainerException("Failed to pull " + image, e);
    } catch (ExecutorStatusCallbackException e) {
      logger.error("Failed to call status callback", e);
      throw new ContainerException("Failed to call status callback", e);
    }
  }

  private String extractServerName(String image) {
    if (StringUtils.countMatches(image, "/") <= 1) {
      return dockerHubServer;
    }
    return image.substring(0, image.indexOf("/"));
  }

  @Override
  public void start() throws ContainerException {
    String dockerPull = dockerResource.getDockerPull();

    try {
      pull(dockerPull);

      Set<String> volumes = new HashSet<>();
      String physicalPath = StorageConfig.getLocalExecutionDirectory(configuration);
      volumes.add(physicalPath);

      ContainerConfig.Builder builder = ContainerConfig.builder();
      builder.image(dockerPull);

      HostConfig.Builder hostConfigBuilder = HostConfig.builder();
      hostConfigBuilder.binds(physicalPath + ":" + physicalPath + ":" + DIRECTORY_MAP_MODE);
      HostConfig hostConfig = hostConfigBuilder.build();
      builder.hostConfig(hostConfig);

      Bindings bindings = BindingsFactory.create(job);
      String commandLine = bindings.buildCommandLine(job);

      if (StringUtils.isEmpty(commandLine.trim())) {
        overrideResultStatus = 0; // default is success
        return;
      }

      File commandLineFile = new File(workingDir, COMMAND_FILE);
      FileUtils.writeStringToFile(commandLineFile, commandLine);
      builder.workingDir(workingDir.getAbsolutePath()).volumes(volumes).cmd("sh", "-c", commandLine);

      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      EnvironmentVariableRequirement environmentVariableResource = getRequirement(combinedRequirements,
          EnvironmentVariableRequirement.class);
      if (environmentVariableResource != null) {
        builder.env(transformEnvironmentVariables(environmentVariableResource.getVariables()));
      }
      ContainerCreation creation = null;
      try {
        VerboseLogger.log(String.format("Running command line: %s", commandLine));
        creation = dockerClient.createContainer(builder.build());
      } catch (DockerException | InterruptedException e) {
        logger.error("Failed to create Docker container.", e);
        throw new ContainerException("Failed to create Docker container.");
      }
      containerId = creation.id();
      try {
        dockerClient.startContainer(containerId);
      } catch (DockerException | InterruptedException e) {
        logger.error("Failed to start Docker container " + containerId, e);
        throw new ContainerException("Failed to start Docker container " + containerId);
      }
      logger.info("Docker container {} has started.", containerId);
    } catch (IOException e) {
      logger.error("Failed to create cmd.log file.", e);
      throw new ContainerException("Failed to create cmd.log file.");
    } catch (Exception e) {
      logger.error("Failed to start container.", e);
      throw new ContainerException("Failed to start container.", e);
    }
  }

  private List<String> transformEnvironmentVariables(Map<String, String> variables) {
    List<String> transformed = new ArrayList<>();
    for (Entry<String, String> variableEntry : variables.entrySet()) {
      transformed.add(variableEntry.getKey() + "=" + variableEntry.getValue());
    }
    return transformed;
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
  public void stop() throws ContainerException {
    if (overrideResultStatus != null) {
      return;
    }
    try {
      dockerClient.stopContainer(containerId, 0);
    } catch (Exception e) {
      logger.error("Docker container " + containerId + " failed to stop", e);
      throw new ContainerException("Docker container " + containerId + " failed to stop");
    }
  }

  @JsonIgnore
  public boolean isStarted() throws ContainerException {
    if (overrideResultStatus != null) {
      return true;
    }
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      Date startedDate = containerState.startedAt();
      return startedDate != null;
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  @Override
  @JsonIgnore
  public boolean isRunning() throws ContainerException {
    if (overrideResultStatus != null) {
      return false;
    }
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      return containerState.running();
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  @Override
  @JsonIgnore
  public int getProcessExitStatus() throws ContainerException {
    if (overrideResultStatus != null) {
      return overrideResultStatus;
    }
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      return containerState.exitCode();
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  /**
   * Does after processing (dumps standard error log for now)
   */
  @Override
  public void dumpContainerLogs(final File logFile) throws ContainerException {
    if (overrideResultStatus != null) {
      return;
    }
    logger.debug("Saving standard error files for id={}", job.getId());

    if (logFile != null) {
      try {
        dumpLog(containerId, logFile);
      } catch (Exception e) {
        logger.error("Docker container " + containerId + " failed to create log file", e);
        throw new ContainerException("Docker container " + containerId + " failed to create log file");
      }
    }
  }

  /**
   * Helper method for dumping error logs from Docker to file
   */
  public void dumpLog(String containerId, File logFile) throws DockerException, InterruptedException {
    LogStream errorStream = null;

    FileChannel fileChannel = null;
    FileOutputStream fileOutputStream = null;
    try {
      if (logFile.exists()) {
        logFile.delete();
      }
      logFile.createNewFile();

      fileOutputStream = new FileOutputStream(logFile);
      fileChannel = fileOutputStream.getChannel();

      errorStream = dockerClient.logs(containerId, LogsParam.stderr());
      while (errorStream.hasNext()) {
        LogMessage message = errorStream.next();
        ByteBuffer buffer = message.content();
        fileChannel.write(buffer);
      }
    } catch (FileNotFoundException e) {
      throw new DockerException("File " + logFile + " not found");
    } catch (IOException e) {
      throw new DockerException(e);
    } finally {
      if (errorStream != null) {
        errorStream.close();
      }
      if (fileChannel != null) {
        try {
          fileChannel.close();
        } catch (IOException e) {
          logger.error("Failed to close file channel", e);
        }
      }
      if (fileOutputStream != null) {
        try {
          fileOutputStream.close();
        } catch (IOException e) {
          logger.error("Failed to close file output stream", e);
        }
      }
    }
  }

  public static class DockerClientLockDecorator {

    public final static int RETRY_TIMES = 5;
    
    public final static long SECOND = 1000L;
    public final static long MINUTE = 60 * SECOND;
    public final static long METHOD_TIMEOUT = 10 * MINUTE; // maximize time (it's mostly because of big Docker images)
    public final static long DEFAULT_DOCKER_CLIENT_TIMEOUT = 1000 * SECOND;
    public final static long SLEEP_TIME = 30 * SECOND;
    
    private DockerClient dockerClient;

    @Inject
    public DockerClientLockDecorator(Configuration configuration) throws ContainerException {
      this.dockerClient = createDockerClient(configuration);
    }

    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true, sleepTimeMillis = SLEEP_TIME)
    public synchronized void pull(String image) throws DockerException, InterruptedException {
      dockerClient.pull(image);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true, sleepTimeMillis = SLEEP_TIME)
    public synchronized void pull(String image, AuthConfig authConfig) throws DockerException, InterruptedException {
      dockerClient.pull(image, authConfig);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized ContainerCreation createContainer(ContainerConfig containerConfig) throws DockerException, InterruptedException {
      return dockerClient.createContainer(containerConfig);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized void startContainer(String containerId) throws DockerException, InterruptedException {
      dockerClient.startContainer(containerId);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized void stopContainer(String containerId, int timeToWait) throws DockerException, InterruptedException {
      dockerClient.stopContainer(containerId, timeToWait);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized ContainerInfo inspectContainer(String containerId) throws DockerException, InterruptedException {
      return dockerClient.inspectContainer(containerId);
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized LogStream logs(String containerId, LogsParam... params) throws DockerException, InterruptedException {
      return dockerClient.logs(containerId, params);
    }

    private DockerClient createDockerClient(Configuration configuration) throws ContainerException {
      DockerClient docker = null;
      try {
        DefaultDockerClient.Builder dockerClientBuilder = DefaultDockerClient.fromEnv()
            .connectTimeoutMillis(TimeUnit.MINUTES.toMillis(5)).readTimeoutMillis(TimeUnit.MINUTES.toMillis(5));

        boolean isConfigAuthEnabled = configuration.getBoolean("docker.override.auth.enabled", false);
        if (isConfigAuthEnabled) {
          String username = configuration.getString("docker.username");
          String password = configuration.getString("docker.password");

          AuthConfig authConfig = AuthConfig.builder().username(username).password(password).build();
          dockerClientBuilder.authConfig(authConfig);
        }
        docker = dockerClientBuilder.build();
      } catch (DockerCertificateException e) {
        logger.error("Failed to create Docker client", e);
        throw new ContainerException("Failed to create Docker client", e);
      }
      return docker;
    }

  }

}
