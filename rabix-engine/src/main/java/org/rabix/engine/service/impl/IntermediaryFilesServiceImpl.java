package org.rabix.engine.service.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.LinkRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class IntermediaryFilesServiceImpl implements IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(IntermediaryFilesServiceImpl.class);
  
  private Map<UUID, Map<String, Integer>> files = new ConcurrentHashMap<UUID, Map<String, Integer>>();

  private LinkRecordService linkRecordService;
  private IntermediaryFilesHandler fileHandler;
  
  @Inject
  protected IntermediaryFilesServiceImpl(LinkRecordService linkRecordService, IntermediaryFilesHandler handler) {
    this.linkRecordService = linkRecordService;
    this.fileHandler = handler;
  }

  @Override
  public void addOrIncrement(UUID rootId, FileValue file, Integer usage) {
    Set<String> paths = new HashSet<String>();
    extractPathsFromFileValue(paths, file);
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

  @Override
  public void decrementFiles(UUID rootId, Set<String> checkFiles) {
    Map<String, Integer> filesForRootId = files.get(rootId);
    for(String path: checkFiles) {
      logger.debug("Decrementing file with path={}", path);
      filesForRootId.put(path, filesForRootId.get(path) - 1);
    }
  }
  
  @Override
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
  
  @Override
  public void handleUnusedFiles(Job job){
    fileHandler.handleUnusedFiles(job, getUnusedFiles(job.getRootId()));
  }
  
  @Override
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

  @Override
  public void handleContainerReady(Job containerJob, boolean keepInputFiles) {
    Integer increment = 0;
    if(keepInputFiles && containerJob.isRoot()) {
      increment = 1;
    }
    for(Map.Entry<String, Object> entry : containerJob.getInputs().entrySet()) {
      List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
      if(!files.isEmpty()) {
        Integer count = linkRecordService.findBySource(containerJob.getName(), entry.getKey(), containerJob.getRootId()).size();
        for(FileValue file: files) {
          if(count > 0) {
            addOrIncrement(containerJob.getRootId(), file, count + increment);
          }
        }
      }
    }
  }

  @Override
  public void handleJobCompleted(Job job) {
    if(!job.isRoot()) {
      boolean isScattered = false;
      for (Map.Entry<String, Object> entry : job.getOutputs().entrySet()) {
        List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
        if (!files.isEmpty()) {
          List<LinkRecord> links = linkRecordService.findBySource(job.getName(), entry.getKey(), job.getRootId());
          Integer count = links.size();
          for (LinkRecord link : links) {
            if(link.getDestinationJobId().equals(InternalSchemaHelper.getJobIdFromScatteredId(job.getName())) && InternalSchemaHelper.getScatteredNumber(job.getName()) != null) {
              isScattered = true;
            }
            if(!link.getDestinationJobId().equals(InternalSchemaHelper.ROOT_NAME) && link.getDestinationVarType().equals(LinkPortType.OUTPUT)) {
              count--;
            }
          }
          for (FileValue file : files) {
            if(count > 0) {
              addOrIncrement(job.getRootId(), file, count);
            }
          }
        }
      }
      if(!isScattered) {
        Set<String> inputs = new HashSet<String>();
        for (Map.Entry<String, Object> entry : job.getInputs().entrySet()) {
          List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
          for (FileValue file : files) {
            extractPathsFromFileValue(inputs, file);
          }
        }
        decrementFiles(job.getRootId(), inputs);
        handleUnusedFiles(job);
      }
      dumpFiles();
    }
  }
  

  @Override
  public void handleJobFailed(Job job, Job rootJob, boolean keepInputFiles) {
    Set<String> rootInputs = new HashSet<String>();
    if(keepInputFiles) {
      for(Map.Entry<String, Object> entry : rootJob.getInputs().entrySet()) {
      List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
        for (FileValue file : files) {
          extractPathsFromFileValue(rootInputs, file);
        }
      }
    }
    jobFailed(job.getRootId(), rootInputs);
    handleUnusedFiles(job);
  }
  
  @Override
  public void extractPathsFromFileValue(Set<String> paths, FileValue file) {
    paths.add(file.getPath());
    for(FileValue f: file.getSecondaryFiles()) {
      extractPathsFromFileValue(paths, f);
    }
  }
}
