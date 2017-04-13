package org.rabix.engine.helper;

import java.util.Set;

import org.rabix.bindings.model.FileValue;

public class IntermediaryFilesHelper {
  
  public static void extractPathsFromFileValue(Set<String> paths, FileValue file) {
    paths.add(file.getPath());
    for(FileValue f: file.getSecondaryFiles()) {
      extractPathsFromFileValue(paths, f);
    }
  }
}
