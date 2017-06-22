package org.rabix.backend.slurm.service;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlurmStorageServiceImpl implements SlurmStorageService {

  private final static Logger logger = LoggerFactory.getLogger(SlurmStorageServiceImpl.class);
  private final String sharedFileStorageDir;

  @Inject
  public SlurmStorageServiceImpl(final Configuration configuration) {
    this.sharedFileStorageDir = Paths.get(configuration.getString("rabix.slurm.shareddir")).toAbsolutePath().toString();
  }



  public Path stagingPath(String... args) {
    Path p = buildPath(sharedFileStorageDir, args);
    createDir(p.getParent());
    return p;
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
