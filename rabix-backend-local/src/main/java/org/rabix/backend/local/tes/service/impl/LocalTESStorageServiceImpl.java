package org.rabix.backend.local.tes.service.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.local.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalTESStorageServiceImpl implements TESStorageService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESStorageServiceImpl.class);
  private final LocalFileStorage localFileStorage;
  private final String stagingBase;
  private final String storageBase;

  @Inject
  public LocalTESStorageServiceImpl(final Configuration configuration) {
    this.localFileStorage = new LocalFileStorage(configuration.getString("backend.execution.directory"));
    this.stagingBase = Paths.get(configuration.getString("rabix.tes.staging.base")).toAbsolutePath().toString();
    this.storageBase = configuration.getString("rabix.tes.storage.base");
  }

  // TES requires absolute file URLs, e.g. "file:///path/to/file.txt" instead of "./file.txt"
  // This transforms file locations to this form.
  // This also prefixes the file path (path inside the container) with DOCKER_PATH_PREFIX.
  private FileValue stageFile(final FileValue fileValue) throws BindingException {

    String location = fileValue.getPath();
    String path;
    URI uri;

    try {
      uri = new URI(location);
    } catch (URISyntaxException e) {
      logger.error("Error staging input directory: {}", location);
      throw new BindingException("Could not stage input directory");
    }

    if (uri.getScheme() != null) {
        path = uri.getPath();

    } else {
      // location is not a URI, treat as path on local file system

      if (!Paths.get(location).isAbsolute()) {
        // location is a relative path. Prefix it with workflow execution dir,
        // which is usually the directory the CWL file is in.
        location = Paths.get(localFileStorage.getBaseDir(), location).toAbsolutePath().toString();
      }
      path = location;
      // TODO should make TES/Funnel URI compliant, then this prefix won't be needed
      location = "file://" + location;
    }

    if (!Paths.get(path).isAbsolute()) {
      // "path" is a relative path. Prefix it with workflow execution dir,
      // which is usually the directory the CWL file is in.
      path = Paths.get(localFileStorage.getBaseDir(), path).toAbsolutePath().toString();
    }

    // Prefix path with container base directory
    path = containerPath("inputs", path).toString();

    fileValue.setLocation(location);
    fileValue.setPath(path);
    return fileValue;
  }

  // Essentially, this recursively walks the input files and calls stageFile().
  // Try to keep special logic out of this, it's better if it does only the recursive walk.
  @Override
  public Job transformInputFiles(Job job) throws BindingException {
    try {
      return FileValueHelper.updateInputFiles(job, new FileTransformer() {
        @Override
        public FileValue transform(FileValue fileValue) throws BindingException {

          // transform directory
          if (fileValue instanceof DirectoryValue) {
            DirectoryValue mapped = (DirectoryValue) stageFile(fileValue);

            // Map directory listing
            List<FileValue> directoryListing = mapped.getListing();
            if (directoryListing != null) {
              for (FileValue listingFile : directoryListing) {
                transform(listingFile);
              }
            }
            return mapped;
          }

          // transform file
          List<FileValue> secondaryFiles = fileValue.getSecondaryFiles();
          if (secondaryFiles != null) {
            for (FileValue secondaryFile : secondaryFiles) {
              transform(secondaryFile);
            }
          }
          return stageFile(fileValue);
        }
      });
    } catch (BindingException e) {
      logger.error("Failed to stage input files", e);
      throw new BindingException("Failed to stage input files", e);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> transformOutputFiles(Map<String, Object> result, String jobRootID, String jobID) throws BindingException {
    return (Map<String, Object>) FileValueHelper.updateFileValues(result, (FileValue fileValue) -> {

      String path = fileValue.getPath();
      String outputPrefix = containerPath().toString();
      if (path.startsWith(outputPrefix)) {
        path = path.substring(outputPrefix.length() + 1);
      }
      String location = Paths.get(storageBase, jobRootID, jobID, path).toUri().toString();
      fileValue.setLocation(location);
      fileValue.setPath(location);
      return fileValue;
    });
  }

  public Path stagingPath(String... args) {
    Path p = buildPath(stagingBase, args);
    // TODO check that parent isn't higher than base dir
    createDir(p.getParent());
    return p;
  }

  public Path outputPath(String... args) {
    return buildPath(storageBase, args);
  }

  public Path containerPath(String... args) {
    return buildPath(DOCKER_PATH_PREFIX, args);
  }

  private Path buildPath(String base, String[] args) {
    if (args.length == 0) {
      return Paths.get(base);
    }
    String first = args[0];
    Path path = Paths.get(base, first);
    for (int i = 1; i < args.length; i++) {
      path = Paths.get(path.toString(), args[i]);
    }
    return path;
  }

  private File createDir(Path path) {
    File dir = path.toFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }
}
