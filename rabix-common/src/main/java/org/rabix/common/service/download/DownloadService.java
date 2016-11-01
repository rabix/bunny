package org.rabix.common.service.download;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface DownloadService {

  public static class DownloadResource {
    private String path;
    private String name;
    private String location;
    private boolean isDirectory;
    
    public DownloadResource(String location, String path, String name, boolean isDirectory) {
      this.path = path;
      this.location = location;
      this.name = name;
      this.isDirectory = isDirectory;
    }
    
    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
      this.location = location;
    }

    public boolean isDirectory() {
      return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
      this.isDirectory = isDirectory;
    }

    @Override
    public String toString() {
      return "ResourceNamePair [path=" + path + ", name=" + name + ", location=" + location + ", isDirectory=" + isDirectory + "]";
    }
  }
  
  void download(File workingDir, DownloadResource resourceNamePair, Map<String, Object> config) throws DownloadServiceException;
  
  void download(File workingDir, Set<DownloadResource> resourceNamePairs, Map<String, Object> config) throws DownloadServiceException;
  
}
