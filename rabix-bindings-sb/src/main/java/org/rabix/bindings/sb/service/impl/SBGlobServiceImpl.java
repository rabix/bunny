package org.rabix.bindings.sb.service.impl;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;
import org.rabix.bindings.sb.service.SBGlobException;
import org.rabix.bindings.sb.service.SBGlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SBGlobServiceImpl implements SBGlobService {

  private final Logger logger = LoggerFactory.getLogger(SBGlobServiceImpl.class);

  /**
   * Find all files that match GLOB inside the working directory 
   */
  public Set<File> glob(SBJob job, File workingDir, Object glob) throws SBGlobException {
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(workingDir);

    Set<File> files = new HashSet<File>();
    if (SBExpressionBeanHelper.isExpression(glob)) {
      try {
        glob = SBExpressionBeanHelper.<String> evaluate(job, glob);
      } catch (SBExpressionException e) {
        logger.error("Failed to evaluate glob " + glob, e);
        throw new SBGlobException("Failed to evaluate glob " + glob, e);
      }
    }
    if (glob == null) {
      return Collections.<File> emptySet();
    }
    
    List<File> globDir = new ArrayList<File>();
    globDir.add(workingDir);
    
    resolveGlob(glob, globDir, files);
    return files;
  }

  private Set<File> listDir(String glob, final boolean isDir, List<File> globDirs) throws SBGlobException {
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
            if(dir.getFileName() != null) {
              if (matcher.matches(dir.getFileName()) && isDir) {
                files.add(dir.toFile());
              }
            }
            return super.preVisitDirectory(dir, attrs);
          }

        });
      } catch (IOException e) {
        logger.error("Failed to traverse through working directory", e);
        throw new SBGlobException("Failed to traverse through working directory", e);
      }
    }
    return files;
  }
  
  public String[] extractGlobParts(String glob) {
    String[] globParts = glob.split("/");
    if(globParts.length < 2) {
      globParts = Arrays.copyOf(globParts, globParts.length+1);
      globParts[globParts.length-1] = "*";
    }
    return globParts;
  }
  
  @SuppressWarnings("unchecked")
  public void resolveGlob(Object glob, List<File> globDirs, Set<File> result) throws SBGlobException {
    if (glob == null) {
      // do nothing
      return;
    }
    else if (glob instanceof List) {
      resolveListGlob((List<Object>) glob, globDirs, result);
    }
    else if (glob instanceof String && ((String) glob).startsWith("{") && ((String) glob).endsWith("}")) {
      resolveMultiGlob((String) glob, globDirs, result);
    }
    else if (glob instanceof String && ((String) glob).contains("/")) {
      resolveFullPathGlob((String) glob, globDirs, result); 
    }
    else if (glob instanceof String) {
      resolveSimpleGlob((String) glob, globDirs, result);
    }
    else {
      logger.debug("Not handled - should never happened");
    }
  }
  
  public void resolveFullPathGlob(String glob, List<File> globDirs, Set<File> result) throws SBGlobException {
    if (glob.startsWith("/")) {
      // handle absolute path
      String[] globParts = glob.split("/");
      String rootGlob = globParts[1];
      File rootDir = new File("/");
      
      File.listRoots();
      List<File> globDir = new ArrayList<File>();
      globDir.add(rootDir);
      if(globParts.length < 2) {
        resolveGlob(rootGlob, globDir, result);
      }
      else {
        Set<File> dirs = listDir(rootGlob, true, globDir);
        String [] globRestParts = Arrays.copyOfRange(globParts, 2, globParts.length);
        String globRest = StringUtils.join(globRestParts, "/");
        List<File> listDirs = new ArrayList<File>();
        listDirs.addAll(dirs);
        resolveGlob(globRest, listDirs, result);
      }
    }
    else {
      // handle relative paths
      String[] globParts = glob.split("/");
      String rootGlob = globParts[0];
      List<File> newGlobDirs;
      if (rootGlob.equals("..")) {
        newGlobDirs = new ArrayList<File>();
        for(File dir: globDirs) {
          newGlobDirs.add(dir.getParentFile());
        }
        if (globParts.length < 2) {
          resolveGlob("*", newGlobDirs, result);
        }
        else {
          String [] globRestParts = Arrays.copyOfRange(globParts, 1, globParts.length);
          String globRest = StringUtils.join(globRestParts, "/");
          resolveGlob(globRest, newGlobDirs, result);
        }
      }
      else if (rootGlob.equals(".")) {
        newGlobDirs = globDirs;
        if (globParts.length < 2) {
          resolveGlob("*", newGlobDirs, result);
        }
        else {
          String [] globRestParts = Arrays.copyOfRange(globParts, 1, globParts.length);
          String globRest = StringUtils.join(globRestParts, "/");
          resolveGlob(globRest, newGlobDirs, result);
        }
      }
      else {
        Set<File> dirs = listDir(rootGlob, true, globDirs);
        if(globParts.length < 2) {
          resolveGlob(rootGlob, globDirs, result);
        }
        else {
          String [] globRestParts = Arrays.copyOfRange(globParts, 1, globParts.length);
          String globRest = StringUtils.join(globRestParts, "/");
          List<File> listDirs = new ArrayList<File>();
          listDirs.addAll(dirs);
          resolveGlob(globRest, listDirs, result);
        }
      }
    }
  }
  
  public void resolveSimpleGlob(String glob, List<File> globDirs, Set<File> result) throws SBGlobException {
    Set<File> dirs = listDir(glob, true, globDirs);
    for(File dir: dirs) {
      List<File> globDir = new ArrayList<File>();
      globDir.add(dir);
      result.addAll(listDir("*", false, globDir));
    }
    if(glob.startsWith("!(") && glob.endsWith(")")) {
      Set<File> exclude = listDir(glob.substring(2, glob.length()-1), false, globDirs);
      Set<File> all = listDir("*", false, globDirs);
      all.removeAll(exclude);
      result.addAll(all);
    }
    else {
      result.addAll(listDir(glob, false, globDirs));
    }
  }
  
  public void resolveMultiGlob(String glob, List<File> globDirs, Set<File> result) throws SBGlobException {
    List<Object> globs = new ArrayList<>();
    String globRemoveBracket = ((String) glob).substring(1, ((String) glob).length()-1);
    for(String globItem: globRemoveBracket.split(",")) {
      globs.add(globItem);
    }
    for(Object singleGlob: globs) {
      resolveGlob(singleGlob, globDirs, result);
    }
  }
  
  public void resolveListGlob(List<Object> globs, List<File> globDirs, Set<File> result) throws SBGlobException {
    for(Object glob: globs) {
      resolveGlob(glob, globDirs, result);
    }
  }
  
}
