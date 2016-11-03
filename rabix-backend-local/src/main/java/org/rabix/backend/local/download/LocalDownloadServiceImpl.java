package org.rabix.backend.local.download;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;

import com.google.inject.Inject;

public class LocalDownloadServiceImpl implements DownloadService {

  private Configuration configuration;

  @Inject
  public LocalDownloadServiceImpl(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public void download(File workingDir, DownloadResource downloadResource, Map<String, Object> config) throws DownloadServiceException {
    String location = downloadResource.getLocation();
    if (location == null) {
      return;
    }
    
    String name = downloadResource.getName();
    if (StringUtils.isEmpty(name)) {
      return;
    }
    if (location.endsWith(name)) {
      return;
    }
    
    File file;
    if (location.startsWith("/")) {
      file = new File(location);
    } else {
      file = new File(new File(configuration.getString("backend.execution.directory")), location);
    }

    File stagedFile = new File(file.getParentFile(), name);
    try {
      if (file.isFile()) {
        FileUtils.copyFile(file, stagedFile);
      } else {
        FileUtils.copyDirectory(file, stagedFile);
      }
      downloadResource.setPath(stagedFile.getPath());
    } catch (IOException e) {
      throw new DownloadServiceException(e.getMessage());
    }
  }

  @Override
  public void download(File workingDir, Set<DownloadResource> downloadResources, Map<String, Object> config) throws DownloadServiceException {
    for (DownloadResource resourceNamePair : downloadResources) {
      download(workingDir, resourceNamePair, config);
    }
  }

}
