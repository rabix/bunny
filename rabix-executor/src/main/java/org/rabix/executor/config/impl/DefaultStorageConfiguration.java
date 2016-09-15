package org.rabix.executor.config.impl;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.executor.config.StorageConfiguration;

import com.google.inject.Inject;

public class DefaultStorageConfiguration implements StorageConfiguration {

  private Configuration configuration;
  
  @Inject
  public DefaultStorageConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public File getWorkingDir(Job job) {
    File contextDir = new File(getPhysicalExecutionBaseDir(), job.getRootId());
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    
    File workingDir = contextDir;
    String[] idArray = transformLocalIDsToPath(job);

    for (String id : idArray) {
      workingDir = new File(workingDir, sanitize(id));
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
    }
    return workingDir;
  }
  
  @Override
  public File getRootDir(String rootId, Map<String, Object> config) {
    File contextDir = new File(getPhysicalExecutionBaseDir(), rootId);
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    return contextDir;
  }
  
  @Override
  public File getWorkingDirWithoutRoot(Job job) {
    File workingDir = new File("/");
    String[] idArray = transformLocalIDsToPath(job);

    for (String id : idArray) {
      workingDir = new File(workingDir, sanitize(id));
    }
    return workingDir;
  }
  
  @Override
  public File getPhysicalExecutionBaseDir() {
    String backendExecutionDirectory = configuration.getString("backend.execution.directory");
    return new File(backendExecutionDirectory);
  }
  
  private String[] transformLocalIDsToPath(Job job) {
    String nodeId = job.getName();
    return nodeId.split("\\" + InternalSchemaHelper.SEPARATOR);
  }
  
  public BackendStore getBackendStore() {
    String backendStore = configuration.getString("backend.store");
    if (backendStore == null || backendStore.isEmpty()) {
      backendStore = BackendStore.LOCAL.name();
    }
    
    for (BackendStore backendStoreEnum : BackendStore.values()) {
      if (backendStore.trim().equalsIgnoreCase(backendStoreEnum.name())) {
        return backendStoreEnum;
      }
    }
    throw new RuntimeException("Invalid backend.store value " + backendStore);
  }
  
  /**
   * Normalize application ID
   */
  private static String sanitize(String id) {
    id = id.replace("@", "_");
    id = id.replace("/", "_");
    id = id.replace("^", "_");
    id = id.replace(":", "_");
    return id.replaceAll("_+", "_");
  }

}
