package org.rabix.executor.config;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface StorageConfiguration {

  public static enum BackendStore {
    VAPOR,
    LOCAL,
    FTP
  }

  File getWorkingDir(Job job);
  
  File getRootDir(UUID rootId, Map<String, Object> config);
  
  File getWorkingDirWithoutRoot(Job job);
  
  File getPhysicalExecutionBaseDir();
  
  BackendStore getBackendStore();

}