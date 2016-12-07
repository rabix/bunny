package org.rabix.backend.local.tes.service;

import org.rabix.bindings.model.Job;

public interface TESStorageService {

  enum StorageType {
    sharedFile
  }
  
  boolean isSupported() throws TESServiceException;
  
  SharedFileStorage getStorageInfo() throws TESServiceException;
  
  Job stageInputFiles(Job job, final LocalFileStorage localFileStorage, final SharedFileStorage sharedFileStorage);
 
  public static class LocalFileStorage {
    private final String baseDir;

    public LocalFileStorage(String baseDir) {
      this.baseDir = baseDir;
    }

    public String getBaseDir() {
      return baseDir;
    }
  }
  
  public static class SharedFileStorage {
    private final String baseDir;

    public SharedFileStorage(String baseDir) {
      this.baseDir = baseDir;
    }

    public String getBaseDir() {
      return baseDir;
    }

  }
  
}
