package org.rabix.bindings.draft2.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.draft2.service.Draft2GlobException;
import org.rabix.bindings.draft2.service.Draft2GlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class Draft2GlobServiceImpl implements Draft2GlobService {

  private final Logger logger = LoggerFactory.getLogger(Draft2GlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  @SuppressWarnings("unchecked")
  public Set<File> glob(Draft2Job job, File workingDir, Object glob) throws Draft2GlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);

    if (Draft2ExpressionBeanHelper.isExpression(glob)) {
      try {
        glob = Draft2ExpressionBeanHelper.<String> evaluate(job, glob);
      } catch (Draft2ExpressionException e) {
        throw new Draft2GlobException("Failed to evaluate glob " + glob, e);
      }
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

    Set<File> files = new HashSet<File>();

    for (String singleGlob : globs) {
      List<File> globDirs = new ArrayList<File>();
      if (singleGlob.startsWith("/")) {
        File globDir = new File(singleGlob).getParentFile();
        globDirs.add(globDir);
        String globString = new File(singleGlob).getName();
        files.addAll(listDir(globString, false, globDirs));
      } else if (singleGlob.contains("/") && !(singleGlob.startsWith("/"))) {
        String[] splitGlob = singleGlob.split("/");
        globDirs.add(workingDir);
        for (int i = 0; i < splitGlob.length - 1; i++) {
          if (splitGlob[i].equals("..")) {
            List<File> newGlobDirs = new ArrayList<File>();
            for(File dir: globDirs) {
              newGlobDirs.add(dir.getParentFile());
            }
            globDirs = newGlobDirs;
          } else {
            Set<File> newGlobDirs = listDir(splitGlob[i], true, globDirs);
            globDirs.clear();
            for(File dir: newGlobDirs) {
              globDirs.add(dir);
            }
          }
        }
        files.addAll(listDir(splitGlob[splitGlob.length-1], false, globDirs));
      } else {
        globDirs.add(workingDir);
        files.addAll(listDir(singleGlob, false, globDirs));
      }
    }
    return files;
  }

  private Set<File> listDir(String glob, final boolean isDir, List<File> globDirs) throws Draft2GlobException {
    final Set<File> files = new HashSet<File>();
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);  
    
    for(File globDir: globDirs) {
      try {
        Files.walkFileTree(globDir.toPath(), EnumSet.noneOf(FileVisitOption.class), 2, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(file.getFileName()) && !isDir) {
              files.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }
          
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(dir.getFileName()) && isDir) {
              files.add(dir.toFile());
            }
            return super.preVisitDirectory(dir, attrs);
          }

        });
      } catch (IOException e) {
        throw new Draft2GlobException("Failed to traverse through working directory", e);
      }
    }
    return files;
  }

}
