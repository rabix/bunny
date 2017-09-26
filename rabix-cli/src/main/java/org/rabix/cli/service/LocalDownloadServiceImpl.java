package org.rabix.cli.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;

import com.google.inject.Inject;

public class LocalDownloadServiceImpl implements DownloadService {

  @Inject
  private Configuration configuration;

  @Override
  public void download(File workingDir, DownloadResource downloadResource, Map<String, Object> config) throws DownloadServiceException {
    String path2 = downloadResource.getPath();
    if (path2 == null)
      return;

    Path path = Paths.get(path2);

    String location = downloadResource.getLocation();
    String name = downloadResource.getName();
    if (name == null)
      name = path.getFileName().toString();
    if (!Files.exists(path) || !path.endsWith(name)) {
      Path locationPath;
      if (location != null) {
        locationPath = Paths.get(URI.create(location));
      } else {
        locationPath = path;
      }
      try {
        Path resolved = path.getParent().resolve(name);
        downloadFile(locationPath, resolved);
        downloadResource.setPath(resolved.toString());
      } catch (IOException e) {
        throw new DownloadServiceException(e);
      }
    }
  }

  private void downloadFile(Path locationPath, Path resolved) {
    if (!Files.isDirectory(locationPath)) {
      try {
        Files.copy(locationPath, resolved, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      try {
        Files.list(locationPath).forEach(f -> downloadFile(f, resolved.resolve(locationPath.relativize(f))));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  public void download(File workingDir, Set<DownloadResource> downloadResources, Map<String, Object> config) throws DownloadServiceException {
    for (DownloadResource resourceNamePair : downloadResources) {
      download(workingDir, resourceNamePair, config);
    }
  }
}
