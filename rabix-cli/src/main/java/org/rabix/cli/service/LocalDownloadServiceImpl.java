package org.rabix.cli.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
      Path resolved = path.getParent().resolve(name);
      if(!downloadFile(locationPath, resolved)){
        throw new DownloadServiceException("Failed to download resource: " + downloadResource.toString());
      }
      downloadResource.setPath(resolved.toString());
    }
  }

  private boolean downloadFile(Path locationPath, Path resolved) {
    if (!Files.isDirectory(locationPath)) {
      try {
        if (!Files.exists(resolved.getParent()))
          Files.createDirectories(resolved);
        Files.copy(locationPath, resolved, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        return false;
      }
    } else {
      try {
        List<Boolean> all = Files.list(locationPath).map(f -> downloadFile(f, resolved.resolve(locationPath.relativize(f)))).collect(Collectors.toList());
        return all.stream().reduce(true, (x, y) -> x && y);
      } catch (IOException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void download(File workingDir, Set<DownloadResource> downloadResources, Map<String, Object> config) throws DownloadServiceException {
    for (DownloadResource resourceNamePair : downloadResources) {
      download(workingDir, resourceNamePair, config);
    }
  }
}
