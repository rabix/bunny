package org.rabix.bindings.cwl.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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

import com.google.common.base.Preconditions;

public class CWLGlobServiceImpl implements CWLGlobService {

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
    final Path workingDirPath = Paths.get(workingDir.getAbsolutePath());
    for (String singleGlob : globs) {
      if (singleGlob.equals(".")) {
        files.add(workingDir);
        continue;
      }
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + singleGlob);
      final Path startDir = workingDir.toPath();
      try {
        Files.walkFileTree(workingDir.toPath(), new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if(!file.startsWith(workingDirPath))
              return FileVisitResult.CONTINUE;

            Path pathRelativeToWorkingDir = file.subpath(workingDirPath.getNameCount(), file.getNameCount());

            if (matcher.matches(pathRelativeToWorkingDir)) {
              files.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(!dir.startsWith(workingDirPath) || startDir.equals(dir))
              return FileVisitResult.CONTINUE;

            Path pathRelativeToWorkingDir = dir.subpath(workingDirPath.getNameCount(), dir.getNameCount());

            if (matcher.matches(pathRelativeToWorkingDir)) {
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
        throw new CWLGlobException("Failed to traverse through working directory", e);
      }
    }
    return files.isEmpty() ? null : files;
  }
  
}
