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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
  public List<File> glob(CWLJob job, File workingDir, Object glob) throws CWLGlobException {
    return this.glob(job, workingDir.toPath(), glob).stream().map(p->p.toFile()).collect(Collectors.toList());
  }
  
  @SuppressWarnings("unchecked")
  public List<Path> glob(CWLJob job, Path workingDir, Object glob) throws CWLGlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);

    try {
      glob = CWLExpressionResolver.resolve(glob, job, null);
    } catch (CWLExpressionException e) {
      throw new CWLGlobException("Failed to evaluate glob " + glob, e);
    }
    if (glob == null) {
      return Collections.<Path> emptyList();
    }
    List<String> globs = new ArrayList<>();
    if (glob instanceof List<?>) {
      globs = (List<String>) glob;
    } else {
      globs.add((String) glob);
    }
    final List<Path> output = new ArrayList<>();
    for (String singleGlob : globs) {
      Set<Path> files = new TreeSet<>();
      if (singleGlob.equals(".")) {
        output.add(workingDir);
        continue;
      }
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + singleGlob);
      try {
        Files.walkFileTree(workingDir, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if(!file.toAbsolutePath().startsWith(workingDir))
              return FileVisitResult.CONTINUE;

            Path pathRelativeToWorkingDir = file.subpath(workingDir.getNameCount(), file.getNameCount());

            if (matcher.matches(pathRelativeToWorkingDir)) {
              files.add(file);
            }
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(!dir.toAbsolutePath().startsWith(workingDir) || workingDir.equals(dir))
              return FileVisitResult.CONTINUE;

            Path pathRelativeToWorkingDir = dir.subpath(workingDir.getNameCount(), dir.getNameCount());

            if (matcher.matches(pathRelativeToWorkingDir)) {
              files.add(dir);
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
      output.addAll(files);
    }
    return output.isEmpty() ? null : output;
  }
}
