package org.rabix.engine.service.impl;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class IntermediaryFilesLocalHandler implements IntermediaryFilesHandler {
  
  private final static Logger logger = LoggerFactory.getLogger(IntermediaryFilesLocalHandler.class);
  
  @Inject
  private IntermediaryFilesLocalHandler() {
  }
  
  @Override
  public void handleUnusedFiles(Job job, Set<String> unusedFiles) {
    for (String p : unusedFiles) {
      Path path = Paths.get(p);
      try {
        logger.info("Deleting file={}", path);
        Files.delete(path);
      } catch (NoSuchFileException e1) {
        logger.error("Failed to delete file={} No such file", path, e1);
        System.err.format("%s: no such" + " file or directory%n", path);
      } catch (DirectoryNotEmptyException e2) {
        logger.error("Failed to delete file={}", path, e2);
      } catch (IOException e3) {
        logger.error("Failed to delete file={}", path, e3);
      }
    }
  }

}