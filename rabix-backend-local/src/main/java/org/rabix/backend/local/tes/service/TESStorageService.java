package org.rabix.backend.local.tes.service;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

public interface TESStorageService {

  public final static String DOCKER_PATH_PREFIX = "/mnt";
  
  enum StorageType {
    Local
  }

  Job transformInputFiles(Job job) throws BindingException;
  Map<String, Object> transformOutputFiles(Map<String, Object> result, String jobID) throws BindingException;
  Path outputPath(String first, String... more);
  Path stagingPath(String first, String... more);
  Path containerPath(String first, String... more);
 
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
