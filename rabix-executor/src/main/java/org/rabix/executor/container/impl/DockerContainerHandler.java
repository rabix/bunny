package org.rabix.executor.container.impl;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.common.retry.Retry;
import org.rabix.executor.config.DockerConfigation;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.handler.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * Docker based implementation of {@link ContainerHandler}
 */
public class DockerContainerHandler implements ContainerHandler {

  private static final Logger logger = LoggerFactory.getLogger(DockerContainerHandler.class);
  
  private static final String dockerHubServer = "https://index.docker.io/v1/";

  public static final String DIRECTORY_MAP_MODE = "rw";

  public static final String HOME_ENV_VAR = "HOME";
  public static final String TMPDIR_ENV_VAR = "TMPDIR";
  
  private static final String TAG_SEPARATOR = ":";
  private static final String LATEST = "latest";
  private static final String SEPARATOR = " ";
  
  private String containerId;
  private DockerClientLockDecorator dockerClient;

  private final Job job;
  private final DockerContainerRequirement dockerResource;

  private final File workingDir;

  private boolean isConfigAuthEnabled;
  private boolean removeContainers;

  private Integer overrideResultStatus = null;

  private StorageConfiguration storageConfig;
  private WorkerStatusCallback statusCallback;
  
  private String commandLine;

  private boolean setPermissions;
  private String user;
  
  private FilePathMapper mapper;
  
  public DockerContainerHandler(Job job, Configuration configuration, DockerContainerRequirement dockerResource, StorageConfiguration storageConfig, DockerConfigation dockerConfig, WorkerStatusCallback statusCallback, DockerClientLockDecorator dockerClient) throws ContainerException {
    this.job = job;
    this.dockerClient = dockerClient;
    this.dockerResource = dockerResource;
    this.statusCallback = statusCallback;
    this.storageConfig = storageConfig;
    this.workingDir = storageConfig.getWorkingDir(job);
    this.isConfigAuthEnabled = dockerConfig.isDockerConfigAuthEnabled();
    this.removeContainers = dockerConfig.removeContainers();

    this.setPermissions = configuration.getBoolean("executor.set_permissions", false);
    
    if (setPermissions) {
      user = configuration.getString("executor.permission.uid");
      String gid = configuration.getString("executor.permission.gid");
      if (gid != null)
        user = user + ":" + gid;
    }
    
    if (SystemUtils.IS_OS_WINDOWS) {
      mapper = (String path, Map<String, Object> config) -> {
        return path.startsWith("/") ? path : "/" + path.replace(":", "").replace("\\", "/");
      };
    } else {
      mapper = (String path, Map<String, Object> config) -> {
        return path;
      };
    }
  }

  private void pull(String image) throws ContainerException {
    logger.debug("Pulling docker image");
    VerboseLogger.log(String.format("Pulling docker image %s", image));

    try {
      statusCallback.onContainerImagePullStarted(job, image);
      if (isConfigAuthEnabled) {
        dockerClient.pull(image);
      } else {
        try {
          String serverAddress = extractServerName(image);
          AuthConfig authConfig = AuthConfig.fromDockerConfig(serverAddress).build();
          this.dockerClient.pull(image, authConfig);
        } catch (IOException | RuntimeException e) {
          logger.debug("Can't find docker config file", e);
          dockerClient.pull(image);
        }
      }
      statusCallback.onContainerImagePullCompleted(job, image);
    } catch (DockerException | InterruptedException e) {
      logger.error("Failed to pull " + image, e);
      throw new ContainerException("Failed to pull " + image, e);
    } catch (WorkerStatusCallbackException e) {
      logger.error("Failed to call status callback", e);
      try {
        statusCallback.onContainerImagePullFailed(job, image);
      } catch (WorkerStatusCallbackException e1) {
        logger.error("Failed to call status callback", e1);
      }
      throw new ContainerException("Failed to call status callback", e);
    }
  }

  private String extractServerName(String image) {
    if (StringUtils.countMatches(image, "/") <= 1) {
      return dockerHubServer;
    }
    return image.substring(0, image.indexOf("/"));
  }
  
  private String checkTagOrAddLatest(String image) {
    return image.contains(TAG_SEPARATOR) ? image : image + TAG_SEPARATOR + LATEST;
  }

  private String getUser() throws IOException {
    if (user == null) {
      user = getId("u");
      String gid = getId("g");
      if (gid != null) {
        user = user + ":" + gid;
      }
    }
    return user;
  }

  private String getId(String param) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStream stream = Runtime.getRuntime().exec(sb.append("id -").append(param).append(" ").append(System.getProperty("user.name")).toString()).getInputStream();
    @SuppressWarnings("unchecked")
    List<String> readLines = IOUtils.readLines(stream);
    return readLines.get(0);
  }
  
  @Override
  public void start() throws ContainerException {
    String dockerPull = checkTagOrAddLatest(dockerResource.getDockerPull());
    
    try {
      pull(dockerPull);
      
      Set<String> volumes = new HashSet<>();
      String physicalPath = storageConfig.getPhysicalExecutionBaseDir().getAbsolutePath();
      volumes.add(physicalPath);

      ContainerConfig.Builder builder = ContainerConfig.builder();
      builder.image(dockerPull);

      HostConfig.Builder hostConfigBuilder = HostConfig.builder();
      volumes = normalizeVolumes(job, volumes);
      
      if(setPermissions && !SystemUtils.IS_OS_WINDOWS){
        builder.user(getUser());
        hostConfigBuilder.capAdd("DAC_OVERRIDE");
      }
      
      Set<String> toBindSet = new HashSet<>();
      toBindSet.addAll(volumes);
      if(dockerResource.getDockerOutputDirectory() != null) {
        volumes.add(dockerResource.getDockerOutputDirectory());
        hostConfigBuilder.binds(workingDir + ":" + dockerResource.getDockerOutputDirectory() + ":" + DIRECTORY_MAP_MODE);
        toBindSet.remove(workingDir);
        toBindSet.remove(dockerResource.getDockerOutputDirectory());
        toBindSet.remove(physicalPath);
      }
      

      if (SystemUtils.IS_OS_WINDOWS) {
        Function<? super String, ? extends String> mapper2 = input -> input.replace("\\", "/");
        toBindSet = toBindSet.stream().map(input -> input + ":/" + input.replace(":", "") + ":" + DIRECTORY_MAP_MODE).map(mapper2).collect(Collectors.toSet());
        volumes = volumes.stream().map(mapper2).collect(Collectors.toSet());
      } else {
        toBindSet = toBindSet.stream().map(input -> input + ":" + input + ":" + DIRECTORY_MAP_MODE).collect(Collectors.toSet());
      }
      
      hostConfigBuilder.binds(new ArrayList<String>(toBindSet));
      
      HostConfig hostConfig = hostConfigBuilder.build();
      builder.hostConfig(hostConfig);
      
      Bindings bindings = BindingsFactory.create(job);
      commandLine = bindings.buildCommandLineObject(job, workingDir, mapper).build();
      
      if (commandLine.startsWith("/bin/bash -c")) {
        commandLine = normalizeCommandLine(commandLine.replace("/bin/bash -c", ""));
        builder.entrypoint("/bin/bash");
      } else if (commandLine.startsWith("/bin/sh -c")) {
        commandLine = normalizeCommandLine(commandLine.replace("/bin/sh -c", ""));
        builder.entrypoint("/bin/sh");
      } else {
        commandLine = normalizeCommandLine(commandLine);
        builder.entrypoint("/bin/sh");
      }
      
      ImageInfo image = dockerClient.inspectImage(dockerPull);
      List<String> entrypoint = image.containerConfig().entrypoint();

      commandLine = addEntrypoint(entrypoint, commandLine);
      
      if (StringUtils.isEmpty(commandLine.trim())) {
        overrideResultStatus = 0; // default is success
        return;
      }

      builder.workingDir(mapper.map(workingDir.getAbsolutePath(), null)).volumes(volumes).cmd("-c", commandLine);
        
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      EnvironmentVariableRequirement environmentVariableResource = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
      Map<String, String> environmentVariables = environmentVariableResource != null ? environmentVariableResource.getVariables() : new HashMap<String, String>();
      Resources resources = job.getResources();
      if(resources != null) {
        if(resources.getWorkingDir() != null) {
          environmentVariables.put(HOME_ENV_VAR, resources.getWorkingDir());
        }
        if(resources.getTmpDir() != null) {
          environmentVariables.put(TMPDIR_ENV_VAR, resources.getTmpDir());
        }
      }
      
      builder.env(transformEnvironmentVariables(environmentVariables));
      try {
        VerboseLogger.log(String.format("Running command line: %s", commandLine));
        containerId = dockerClient.createContainer(builder.build());
      } catch (DockerException | InterruptedException e) {
        logger.error("Failed to create Docker container.", e);
        throw new ContainerException("Failed to create Docker container.");
      }
      try {
        dockerClient.startContainer(containerId);
      } catch (Exception e) {
        logger.error("Failed to start Docker container " + containerId, e);
        throw new ContainerException("Failed to start Docker container " + containerId);
      }
      logger.info("Docker container {} has started.", containerId);
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
  
  private String addEntrypoint(List<String> entrypoint, String commandLine) {
    if (entrypoint != null && !entrypoint.isEmpty()) {
      String entryPointString = null;
      for (String part : entrypoint) {
        if (entryPointString == null) {
          entryPointString = part + " ";
        } else {
          entryPointString = entryPointString + part + SEPARATOR;
        }
      }
      commandLine = entryPointString + commandLine;
    }
    return commandLine;
  }
  
  private Set<String> normalizeVolumes(Job job, Set<String> volumes) throws BindingException {
    Set<String> paths = new HashSet<>();
    Set<FileValue> files = flattenFiles(FileValueHelper.getInputFiles(job));
    
    for (FileValue fileValue : files) {
      paths.add(Paths.get(fileValue.getPath()).getParent().toAbsolutePath().toString());
    }
    paths.addAll(volumes);
    
    List<String> toRemove = new ArrayList<>();
    for (String pathA : paths) {
      for (String pathB : paths) {
        if (pathA.equals(pathB)) {
          continue;
        }
        if (pathB.startsWith(pathA)) {
          toRemove.add(pathB);
        }
      }
    }
    for (String path : toRemove) {
      paths.remove(path);
    }
    return paths;
  }
  
  private Set<FileValue> flattenFiles(Set<FileValue> fileValues) {
    Set<FileValue> flattenedFileValues = new HashSet<>();
    for (FileValue fileValue : fileValues) {
      flattenedFileValues.add(fileValue);
      if (fileValue.getSecondaryFiles() != null) {
        flattenedFileValues.addAll(fileValue.getSecondaryFiles());
      }
    }
    return flattenedFileValues;
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
  
  @Override
  public void removeContainer() {
    try {
      if(removeContainers) {
        logger.debug("Removing container with id " + containerId);
        dockerClient.removeContainer(containerId);
      }
    } catch (Exception e) {
      logger.error("Failed to remove container with id " + containerId, e);
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
    
    public static final String DOCKER_HOST_ENVVAR = "DOCKER_HOST";
    public static final String DOCKER_HOST_CONFIG = "docker.host";
    public static final String DOCKER_CERT_PATH_CONFIG = "docker.certpath";
    public static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";
    
    private static final String UNIX_SCHEME = "unix";
    
    private DockerClient dockerClient;

    @Inject
    public DockerClientLockDecorator(Configuration configuration) throws ContainerException {
      this.dockerClient = createDockerClient(configuration);
    }
    
    public synchronized void removeContainer(String containerId) throws DockerException, InterruptedException {
      dockerClient.removeContainer(containerId);
    }

    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = false, sleepTimeMillis = SLEEP_TIME)
    public synchronized void pull(String image) throws DockerException, InterruptedException {
      try {
        if(!imageExists(image)) {
          dockerClient.pull(image);
        }
      } catch (Throwable e) {
        VerboseLogger.log("Failed to pull docker image. Retrying in " + TimeUnit.MILLISECONDS.toSeconds(SLEEP_TIME) + " seconds");
        throw e;
      }
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = false, sleepTimeMillis = SLEEP_TIME)
    public synchronized void pull(String image, AuthConfig authConfig) throws DockerException, InterruptedException {
      try {
        if(!imageExists(image)) {
          dockerClient.pull(image, authConfig);
        }
      } catch (Throwable e) {
        VerboseLogger.log("Failed to pull docker image. Retrying in " + TimeUnit.MILLISECONDS.toSeconds(SLEEP_TIME) + " seconds");
        throw e;
      }
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized String createContainer(ContainerConfig containerConfig) throws DockerException, InterruptedException {
      return dockerClient.createContainer(containerConfig).id();
    }
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized void startContainer(String containerId) throws DockerException, InterruptedException {
      try {
        dockerClient.startContainer(containerId);
      } catch (DockerException e) {
        ContainerInfo inspect = dockerClient.inspectContainer(containerId);
        if (inspect == null || inspect.state().startedAt() == null) {
          throw e;
        }
        logger.warn("Start container method timed-out but still started the container, recovering.");
      }
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
    
    @Retry(times = RETRY_TIMES, methodTimeoutMillis = METHOD_TIMEOUT, exponentialBackoff = true)
    public synchronized ContainerExit waitContainer(String containerId) throws DockerException, InterruptedException {
      return dockerClient.waitContainer(containerId);
    }
    
    private boolean imageExists(String dockerPull) {
      List<String> images = null;
      try {
        images = listImages();
      } catch (DockerException | InterruptedException e) {
        return false;
      }
      return images != null ? images.contains(dockerPull) : false;
    }
    
    public synchronized ImageInfo inspectImage(String dockerPull) throws DockerException, InterruptedException {
      return dockerClient.inspectImage(dockerPull);
    }
    
    public synchronized List<String> listImages() throws DockerException, InterruptedException {
      List<Image> images = dockerClient.listImages();
      List<String> result = new ArrayList<String>();
      for(Image image: images) {
        if(image.repoTags() != null) {
          result.addAll(image.repoTags());
        }
      }
      return result;
    }
    
    public static DockerClient createDockerClient(Configuration configuration) throws ContainerException {
      DockerClient docker = null;
      DefaultDockerClient.Builder dockerClientBuilder = dockerClientBuilder(configuration);
      boolean isConfigAuthEnabled = configuration.getBoolean("docker.override.auth.enabled", false);
      if (isConfigAuthEnabled) {
        String username = configuration.getString("docker.username");
        String password = configuration.getString("docker.password");

        AuthConfig authConfig = AuthConfig.builder().username(username).password(password).build();
        dockerClientBuilder.authConfig(authConfig);
      }
      docker = dockerClientBuilder.build();
      return docker;
    }
    
    public static DefaultDockerClient.Builder dockerClientBuilder(Configuration configuration) throws ContainerException {
      DefaultDockerClient.Builder dockerClientBuilder = null;
      if(System.getenv().containsKey(DOCKER_HOST_ENVVAR)) {
        try {
          dockerClientBuilder = DefaultDockerClient.fromEnv().connectTimeoutMillis(TimeUnit.MINUTES.toMillis(5)).readTimeoutMillis(TimeUnit.MINUTES.toMillis(5));
          return dockerClientBuilder;
        } catch (DockerCertificateException e) {
          logger.debug("Failed to create Docker client from environment variables", e);
        }
      }
      else if(configuration.containsKey(DOCKER_HOST_CONFIG)) {
        String endpoint = configuration.getString(DOCKER_HOST_CONFIG);
        String dockerCertPath = configuration.containsKey(DOCKER_CERT_PATH_CONFIG) ? configuration.getString(DOCKER_CERT_PATH_CONFIG) : defaultCertPath();
        try {
          dockerClientBuilder = buildDockerClientBuilder(endpoint, dockerCertPath);
          dockerClientBuilder.connectTimeoutMillis(TimeUnit.MINUTES.toMillis(5)).readTimeoutMillis(TimeUnit.MINUTES.toMillis(5));
          return dockerClientBuilder;
        } catch (DockerCertificateException e) {
          logger.error("Failed to create Docker client from configuration", e);
        }
      }
      else {
        try {
          dockerClientBuilder = buildDockerClientBuilder(DEFAULT_DOCKER_HOST, defaultCertPath());
          dockerClientBuilder.connectTimeoutMillis(TimeUnit.MINUTES.toMillis(5)).readTimeoutMillis(TimeUnit.MINUTES.toMillis(5));
        } catch (DockerCertificateException e) {
          logger.error("Failed to create Docker client", e);
          throw new ContainerException("Failed to create Docker client", e);
        }
      }
      return dockerClientBuilder;
    }
    
    private static DefaultDockerClient.Builder buildDockerClientBuilder(String endpoint, String dockerCertPath) throws DockerCertificateException {
      DefaultDockerClient.Builder dockerClientBuilder = DefaultDockerClient.builder();
      final Optional<DockerCertificates> certs = DockerCertificates.builder().dockerCertPath(Paths.get(dockerCertPath)).build();
      
      if (endpoint.startsWith(UNIX_SCHEME + "://")) {
        dockerClientBuilder.uri(endpoint);
      } else {
        final String stripped = endpoint.replaceAll(".*://", "");
        final HostAndPort hostAndPort = HostAndPort.fromString(stripped);
        final String hostText = hostAndPort.getHostText();
        final String scheme = certs.isPresent() ? "https" : "http";

        final int port = hostAndPort.getPort();
        final String address = hostText;
        dockerClientBuilder.uri(scheme + "://" + address + ":" + port);
      }
      if (certs.isPresent()) {
        dockerClientBuilder.dockerCertificates(certs.get());
      }
      return dockerClientBuilder;
    }
    
    private static String defaultCertPath() {
      return Paths.get(getProperty("user.home"), ".docker").toString();
    }
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
  
}