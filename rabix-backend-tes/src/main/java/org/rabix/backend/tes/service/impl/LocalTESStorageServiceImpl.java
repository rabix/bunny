package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.tes.service.TESStorageService;
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

  private final Path localFileStorage;
  private final Path storageBase;

  @Inject
  public LocalTESStorageServiceImpl(Configuration configuration) {
    localFileStorage = Paths.get(configuration.getString("backend.execution.directory"));

    URI uri = URI.create(configuration.getString("rabix.tes.storage.base", localFileStorage.toString()));
    if (uri.getScheme() == null) {
      try {
        uri = new URI("file", uri.toString(), null);
      } catch (URISyntaxException e) {
        logger.error("Failed to parse storage path", e);
      }
    }
    storageBase = Paths.get(uri);
  }

  @Override
  public Job transformInputFiles(Job job) throws BindingException {
    try {
      Path workdir = workDir(job);
      return FileValueHelper.updateInputFiles(job, new FileTransformer() {
        @Override
        public FileValue transform(FileValue fileValue) throws BindingException {
          stageFile(workdir, localDir(job), fileValue);
          return fileValue;
        }
      });
    } catch (BindingException e) {
      logger.error("Failed to stage input files", e);
      throw new BindingException("Failed to stage input files", e);
    }
  }

  private void stageFile(Path workdir, Path localPath, FileValue fileValue) {
    String path = fileValue.getPath();
    String location = fileValue.getLocation();
    URI locationUri;
    Path locationPath;
    if (location == null) {
      locationPath = Paths.get(path);
      locationUri = locationPath.toUri();
      fileValue.setLocation(locationUri.toString());
    } else {
      locationUri = URI.create(location);
      locationPath = Paths.get(locationUri);
    }
    if (location == null || locationUri.getScheme().equals("file")) {
      try {
        Path staged = workdir.getParent().resolve(path.substring(1));
        if (!Files.exists(staged)) {
          Files.copy(locationPath, staged);
          fileValue.setLocation(staged.toUri().toString());
        }

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    fileValue.getSecondaryFiles().forEach(f -> stageFile(workdir, localPath, f));
    if(fileValue instanceof DirectoryValue){
      ((DirectoryValue) fileValue).getListing().stream().forEach(f -> stageFile(workdir, localPath, f));
    }
  }

  @Override
  public Path workDir(Job job) {
    return storageBase.resolve(job.getRootId().toString()).resolve(job.getName().replaceAll("\\.", "/"));
  }  
  
  @Override
  public Path localDir(Job job) {
    return localFileStorage.resolve(job.getRootId().toString()).resolve(job.getName().replaceAll("\\.", "/"));
  }
}
