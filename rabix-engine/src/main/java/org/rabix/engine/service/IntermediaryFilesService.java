package org.rabix.engine.service;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.model.FileValue;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.engine.helper.IntermediaryFilesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(IntermediaryFilesService.class);
  
  private Map<UUID, Map<String, Integer>> files = new ConcurrentHashMap<UUID, Map<String, Integer>>();
  
  public void addOrIncrement(UUID rootId, FileValue file, Integer usage) {
    Set<String> paths = new HashSet<String>();
    IntermediaryFilesHelper.extractPathsFromFileValue(paths, file);
    Map<String, Integer> filesForRootId = files.get(rootId) != null ? files.get(rootId): new HashMap<String, Integer>();
    for(String path: paths) {
      if(filesForRootId.containsKey(path)) {
        logger.debug("Increment file usage counter: " + path + ": " + ((Integer) filesForRootId.get(path) + usage));
        filesForRootId.put(path, filesForRootId.get(path) + usage);
      }
      else {
        logger.debug("Adding file usage counter: " + path + ": " + usage);
        filesForRootId.put(path, usage);
      }
    }
    files.put(rootId, filesForRootId);
  }
  
  protected Set<String> getUnusedFiles(UUID rootId) {
    Map<String, Integer> filesForRootId = files.get(rootId) != null ? files.get(rootId): Collections.<String, Integer>emptyMap();
    Set<String> unusedFiles = new HashSet<String>();
    for(Iterator<Map.Entry<String, Integer>> it = filesForRootId.entrySet().iterator(); it.hasNext();) {
      Entry<String, Integer> entry = it.next();
      if(entry.getValue() == 0) {
        unusedFiles.add(entry.getKey());
        it.remove();
      }
    }
    return unusedFiles;
  }

  
  public void decrementFiles(UUID rootId, Set<String> checkFiles) {
    Map<String, Integer> filesForRootId = files.get(rootId);
    for(String path: checkFiles) {
      logger.debug("Decrementing file with path={}", path);
      filesForRootId.put(path, filesForRootId.get(path) - 1);
    }
  }
  
  public void jobFailed(UUID rootId, Set<String> rootInputs) {
    Map<String, Integer> filesForRootId = files.get(rootId);
    for(Iterator<Map.Entry<String, Integer>> it = filesForRootId.entrySet().iterator(); it.hasNext();) {
      Entry<String, Integer> fileEntry = it.next();
      if(!rootInputs.contains(fileEntry.getKey())) {
        logger.debug("Removing onJobFailed: " + fileEntry.getKey());
        filesForRootId.put(fileEntry.getKey(), 0);
      }
    }
  }
  
  public abstract void handleUnusedFiles(UUID rootId);
  
  public void dumpFiles() {
    VerboseLogger.log("Intermediary files table");
    for(Iterator<Map.Entry<UUID, Map<String, Integer>>> it = files.entrySet().iterator(); it.hasNext();) {
      Entry<UUID, Map<String, Integer>> tableEntry = it.next();
      VerboseLogger.log("RootId: " + tableEntry.getKey());
      for(Iterator<Map.Entry<String, Integer>> itt = tableEntry.getValue().entrySet().iterator(); itt.hasNext();) {
        Entry<String, Integer> fileEntry = itt.next();
        VerboseLogger.log(fileEntry.getKey() + ": " + fileEntry.getValue());
      }
    }
  }
  
}
