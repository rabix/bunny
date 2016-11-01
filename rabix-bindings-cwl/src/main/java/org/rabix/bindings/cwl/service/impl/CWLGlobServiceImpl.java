package org.rabix.bindings.cwl.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.service.CWLGlobException;
import org.rabix.bindings.cwl.service.CWLGlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class CWLGlobServiceImpl implements CWLGlobService {

  private final Logger logger = LoggerFactory.getLogger(CWLGlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  @SuppressWarnings("unchecked")
  public Set<File> glob(CWLJob job, File workingDir, Object glob) throws CWLGlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);
    
    try {
      glob = CWLExpressionResolver.resolve(glob, job, null);
    } catch (CWLExpressionException e) {
      logger.error("Failed to evaluate glob " + glob, e);
      throw new CWLGlobException("Failed to evaluate glob " + glob, e);
    }
    if (glob == null) {
      return Collections.<File> emptySet();
    }
    List<String> globs = new ArrayList<>();
    if (glob instanceof List<?>) {
      globs = (List<String>) glob;
    } else {
      globs.add((String) glob);
    }
    
    final Set<File> files = new LinkedHashSet<>();
    for (String singleGlob : globs) {
      if (singleGlob.equals(".")) { // TODO fix this
        singleGlob = workingDir.getName();
      }
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + singleGlob);
      try {
        Files.walkFileTree(workingDir.toPath(), new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(file.getFileName())) {
              files.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(dir.getFileName())) {
              files.add(dir.toFile());
            }
            return super.preVisitDirectory(dir, attrs);
          }
          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        logger.error("Failed to traverse through working directory", e);
        throw new CWLGlobException("Failed to traverse through working directory", e);
      }
    }
    return files.isEmpty() ? null : files;
  }
  
}
