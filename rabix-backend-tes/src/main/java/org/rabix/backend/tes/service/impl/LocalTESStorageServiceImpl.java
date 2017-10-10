package org.rabix.backend.tes.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.tes.service.TESStorageException;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.FileValue.FileType;
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
      List<FileValue> flat = new ArrayList<>();
      return FileValueHelper.updateInputFiles(job, new FileTransformer() {
        @Override
        public FileValue transform(FileValue fileValue) throws BindingException {
          try {
            stageFile(workdir, fileValue, flat);
          } catch (TESStorageException e) {
            throw new BindingException(e.getMessage());
          }
          return fileValue;
        }
      });
    } catch (BindingException e) {
      logger.error("Failed to stage input files", e);
      throw new BindingException("Failed to stage input files", e);
    }

  }

  public List<FileValue> stageFile(Path workdir, FileValue fileValue) throws TESStorageException {
    ArrayList<FileValue> flat = new ArrayList<>();
    this.stageFile(workdir, fileValue, flat);
    return flat;
  }

  @Override
  public void stageFile(Path workdir, FileValue fileValue, List<FileValue> flat) throws TESStorageException {
//    checkName(fileValue, fileValue.getName());
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
    if ((location == null || locationUri.getScheme().equals("file")) && !fileValue.getType().equals(FileType.Directory)) {
      try {
        Path staged = workdir.resolve("./" + path).normalize();
        if (!Files.exists(staged)) {
          Files.copy(locationPath, staged);
          fileValue.setLocation(staged.toUri().toString());
        }
      } catch (IOException e) {
        throw new TESStorageException(e.getMessage());
      }
    }
    for (FileValue f : fileValue.getSecondaryFiles()) {
      stageFile(workdir, f, flat);
    }
    if (fileValue instanceof DirectoryValue) {
      List<FileValue> listing = ((DirectoryValue) fileValue).getListing();
      for (FileValue f : listing) {
        stageFile(workdir, f, flat);
      }
    }
    flat.add(fileValue);
  }

  private void checkName(FileValue fileValue, String bottom) {
    if (!fileValue.getPath().endsWith(bottom)) {
      Path path = Paths.get(fileValue.getPath());
      Path newPath = path.resolveSibling(bottom);
      fileValue.setPath(newPath.toString());

      if (fileValue instanceof DirectoryValue) {
        ((DirectoryValue) fileValue).getListing().forEach(f -> {
          checkName(f, newPath.resolve(path.relativize(Paths.get(f.getPath()))).toString());
        });
      }
      fileValue.getSecondaryFiles().forEach(f -> f.setPath(newPath.resolve(f.getName()).toString()));
    }
  }

  @Override
  public void downloadDirectory(Path localDir, Path outDir) throws TESStorageException {
    try {
      Files.createDirectories(localDir);
      Path[] array = Files.list(outDir).toArray(Path[]::new);
      for (Path p : array) {
        Path resolved = localDir.resolve(outDir.relativize(p.toAbsolutePath()).toString());
        if (!Files.isDirectory(p)) {
          Files.copy(p.toAbsolutePath(), resolved, StandardCopyOption.REPLACE_EXISTING);
        } else {
          downloadDirectory(resolved, p.toAbsolutePath());
        }
      }
    } catch (IOException e1) {
      throw new TESStorageException(e1);
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
