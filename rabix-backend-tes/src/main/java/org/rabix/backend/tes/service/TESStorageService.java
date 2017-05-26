package org.rabix.backend.tes.service;

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
  Map<String, Object> transformOutputFiles(Map<String, Object> result, String jobRootID, String jobID) throws BindingException;
  Path outputPath(String... args);
  Path stagingPath(String... args);
  Path containerPath(String... args);
 
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
