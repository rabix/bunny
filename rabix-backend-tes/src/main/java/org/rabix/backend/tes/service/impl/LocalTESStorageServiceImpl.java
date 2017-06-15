package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalTESStorageServiceImpl implements TESStorageService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESStorageServiceImpl.class);

  private static final Path containerPath = Paths.get(DOCKER_PATH_PREFIX);
  private final String schema;
  private final Path localFileStorage;
  private final Path storageBase;

  @Inject
  public LocalTESStorageServiceImpl(Configuration configuration) {
    this.localFileStorage = Paths.get(configuration.getString("backend.execution.directory"));
    this.storageBase = Paths.get(URI.create(configuration.getString("rabix.tes.storage.base")));
    schema = storageBase.toUri().getScheme();
  }

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

  @Override
  public Path writeJobFile(Job job) throws IOException {
    Path dir = storageBase.resolve(job.getId().toString());
    Path path = dir.resolve("job.json");
    Files.createDirectories(dir);
    Files.write(path, JSONHelper.writeObject(job).getBytes());
    return path;
  }

  protected FileValue stageFile(FileValue fileValue) {
    fileValue.getSecondaryFiles().stream().forEach(f -> stageFile(f));
    if (fileValue instanceof DirectoryValue)
      ((DirectoryValue) fileValue).getListing().stream().forEach(f -> stageFile(f));

    if (fileValue.getLocation() == null) {
      Path filePath = localFileStorage.resolve(fileValue.getPath());
      URI uri = filePath.toUri();
      if (uri.getScheme() != null && uri.getScheme().equals(schema)) {
        fileValue.setLocation(uri.toString());
      } else {
        try {
          Path staged = storageBase.resolve(fileValue.getPath());
          if (!Files.exists(staged)) {
            Files.copy(Paths.get(uri), staged);
          }
          fileValue.setLocation(staged.toUri().toString());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    fileValue.setPath(containerPath.resolve(fileValue.getPath()).toString());
    return fileValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> transformOutputFiles(Map<String, Object> result, Job job) throws BindingException {
    return (Map<String, Object>) FileValueHelper.updateFileValues(result, (FileValue fileValue) -> {
      String location = output(job, fileValue.getName()).toUri().toString();
      fileValue.setLocation(location);
      return fileValue;
    });
  }

  public Path output(Job job, String filename) {
    return outputPath(job.getRootId().toString(), job.getName(), "working_dir", filename);
  }

  @Override
  public Path outputPath(String... args) {
    return resolveRec(storageBase, args);
  }

  private Path resolveRec(Path path, String... args) {
    Path out = path;
    for (String a : args) {
      out = out.resolve(a);
    }
    return out;
  }

  @Override
  public Path containerPath(String... args) {
    return resolveRec(containerPath, args);
  }
}
