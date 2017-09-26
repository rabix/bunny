package org.rabix.backend.tes.service;

import java.nio.file.Path;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;

public interface TESStorageService {
  
  enum StorageType {
    Local
  }

  Job transformInputFiles(Job job) throws BindingException;
   
  Path workDir(Job job);

  public static class LocalFileStorage {
    private final String baseDir;

    public LocalFileStorage(String baseDir) {
      this.baseDir = baseDir;
    }

    public String getBaseDir() {
      return baseDir;
    }
  }

  Path localDir(Job job);

}
