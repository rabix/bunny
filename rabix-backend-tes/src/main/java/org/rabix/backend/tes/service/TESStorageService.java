package org.rabix.backend.tes.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;

public interface TESStorageService {

  public final static String DOCKER_PATH_PREFIX = "/mnt";
  
  enum StorageType {
    Local
  }

  Job transformInputFiles(Job job) throws BindingException;

  Map<String, Object> transformOutputFiles(Map<String, Object> result, Job job) throws BindingException;

  Path containerPath(String... args);
  
  Path writeJobFile(Job job) throws IOException;
 
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

}
