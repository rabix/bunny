package org.rabix.executor.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.service.FilePermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

public class FilePermissionServiceImpl implements FilePermissionService {

  private final static Logger logger = LoggerFactory.getLogger(FilePermissionServiceImpl.class);

  private final static String IMAGE = "ubuntu:latest";
  private final static String DIRECTORY_MAP_MODE = "rw";

  private final StorageConfiguration storageConfig;
  private final DockerClientLockDecorator dockerClient;

  private final String permissionUID;
  private final String permissionGID;

  @Inject
  public FilePermissionServiceImpl(DockerClientLockDecorator dockerClient, StorageConfiguration storageConfiguration, Configuration configuration) {
    this.dockerClient = dockerClient;
    this.storageConfig = storageConfiguration;
    
    this.permissionUID = configuration.containsKey("executor.permission.uid") ? configuration.getString("executor.permission.uid") : getUid();
    this.permissionGID = configuration.containsKey("executor.permission.gid") ? configuration.getString("executor.permission.gid") : getGid();
  }

  @Override
  public void execute(Job job) throws ContainerException {
    try {
      dockerClient.pull(IMAGE);

      Set<String> volumes = new HashSet<>();
      String physicalPath = storageConfig.getPhysicalExecutionBaseDir().getAbsolutePath();
      volumes.add(physicalPath);

      ContainerConfig.Builder builder = ContainerConfig.builder();
      builder.image(IMAGE);

      HostConfig.Builder hostConfigBuilder = HostConfig.builder();
      hostConfigBuilder.binds(physicalPath + ":" + physicalPath + ":" + DIRECTORY_MAP_MODE);
      HostConfig hostConfig = hostConfigBuilder.build();
      builder.hostConfig(hostConfig);

      File workingDir = storageConfig.getWorkingDir(job);
      String commandLine = getChmodCommand(workingDir) + ";" + getChownCommand(workingDir);
      builder.workingDir(workingDir.getAbsolutePath()).volumes(volumes).cmd("sh", "-c", commandLine);

      ContainerCreation creation = dockerClient.createContainer(builder.build());
      String containerId = creation.id();
      dockerClient.startContainer(containerId);
      logger.info("Docker container {} has started.", containerId);
      dockerClient.waitContainer(containerId);
    } catch (Exception e) {
      logger.error("Failed to start container.", e);
      throw new ContainerException("Failed to start container.", e);
    }
  }

  /**
   * Construct CHMOD sub command
   */
  private String getChmodCommand(File workingDir) {
    String workingDirPath = workingDir.getAbsolutePath();
    if (workingDirPath.endsWith("/")) {
      workingDirPath = workingDirPath.substring(0, workingDirPath.length() - 1);
    }
    return "chmod -R u+r \"" + workingDirPath + "\"/*";
  }

  /**
   * Construct CHOWN sub command
   */
  private String getChownCommand(File workingDir) {
    String workingDirPath = workingDir.getAbsolutePath();

    if (workingDirPath.endsWith("/")) {
      workingDirPath = workingDirPath.substring(0, workingDirPath.length() - 1);
    }
    return "chown -R " + permissionUID + ":" + permissionGID + " \"" + workingDirPath + "\"/*";
  }
  
  public static String getUid() {
    String uid = "";;
    String userName = System.getProperty("user.name");
    String command = "id -u " + userName;
    Process child;
    try {
      child = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      logger.error("Failed to get UID for user", e);
      return null;
    }
    InputStream in = child.getInputStream();;
    try {
      int c;
      while ((c = in.read()) != -1) {
        uid += (char) c;
      }
      return uid.replace(System.getProperty("line.separator"), "");
    } catch (IOException e){
      logger.error("Failed to get UID for user" + userName, e);
      return null;
    }
    finally {
      try {
        in.close();
      } catch (IOException e) {
        logger.error("Failed to get UID for user" + userName, e);
      }
    }
  }
  
  public static String getGid() {
    String uid = "";;
    String userName = System.getProperty("user.name");
    String command = "id -g " + userName;
    Process child;
    try {
      child = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      logger.error("Failed to get UID for user", e);
      return null;
    }
    InputStream in = child.getInputStream();;
    try {
      int c;
      while ((c = in.read()) != -1) {
        uid += (char) c;
      }
      return uid.replace(System.getProperty("line.separator"), "");
    } catch (IOException e){
      logger.error("Failed to get UID for user" + userName, e);
      return null;
    }
    finally {
      try {
        in.close();
      } catch (IOException e) {
        logger.error("Failed to get UID for user" + userName, e);
      }
    }
  }
}
