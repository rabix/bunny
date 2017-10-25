package org.rabix.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.repository.IntermediaryFilesRepository;
import org.rabix.engine.store.repository.IntermediaryFilesRepository.IntermediaryFileEntity;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.LinkRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class IntermediaryFilesServiceImpl implements IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(IntermediaryFilesServiceImpl.class);

  private IntermediaryFilesRepository intermediaryFilesRepository;
  private LinkRecordService linkRecordService;
  private IntermediaryFilesHandler fileHandler;
  
  @Inject
  protected IntermediaryFilesServiceImpl(LinkRecordService linkRecordService, IntermediaryFilesHandler handler, IntermediaryFilesRepository intermediaryFilesRepository) {
    this.linkRecordService = linkRecordService;
    this.fileHandler = handler;
    this.intermediaryFilesRepository = intermediaryFilesRepository;
  }

  @Override
  public void decrementFiles(UUID rootId, Set<String> checkFiles) {
    for (String path : checkFiles) {
      intermediaryFilesRepository.decrement(rootId, path);
    }
  }
  
  @Override
  public void handleUnusedFiles(Job job){
    fileHandler.handleUnusedFiles(job, getUnusedFiles(job.getRootId()));
  }


  @Override
  public void handleJobCompleted(Job job) {
    if (!job.isRoot()) {
      Set<String> inputs = new HashSet<String>();
      for (Map.Entry<String, Object> entry : job.getInputs().entrySet()) {
        Set<FileValue> files = new HashSet(FileValueHelper.getFilesFromValue(entry.getValue()));
        for (FileValue file : files) {
          extractPathsFromFileValue(inputs, file);
        }
      }
      decrementFiles(job.getRootId(), inputs);
      handleUnusedFiles(job);
    }
  }

  @Override
  public void handleJobFailed(Job job, Job rootJob) {
//    Set<String> rootInputs = new HashSet<String>();
//    if(keepInputFiles) {
//      for(Map.Entry<String, Object> entry : rootJob.getInputs().entrySet()) {
//      Set<FileValue> files = new HashSet(FileValueHelper.getFilesFromValue(entry.getValue()));
//        for (FileValue file : files) {
//          extractPathsFromFileValue(rootInputs, file);
//        }
//      }
//    }
//    jobFailed(job.getRootId(), rootInputs); 
    handleUnusedFiles(job);
  }
  
  @Override
  public void jobFailed(UUID rootId, Set<String> rootInputs) {
    List<IntermediaryFileEntity> filesForRootIdList = intermediaryFilesRepository.get(rootId);
    Map<String, Integer> filesForRootId = convertToMap(filesForRootIdList);
    for(Iterator<Map.Entry<String, Integer>> it = filesForRootId.entrySet().iterator(); it.hasNext();) {
      Entry<String, Integer> fileEntry = it.next();
      if(!rootInputs.contains(fileEntry.getKey())) {
        logger.debug("Removing onJobFailed: " + fileEntry.getKey());
        filesForRootId.put(fileEntry.getKey(), 0);
      }
    }
  }
  
  private Map<String, Integer> convertToMap(List<IntermediaryFileEntity> filesForRootId) {
    Map<String, Integer> result = new HashMap<>();
    for(IntermediaryFileEntity f: filesForRootId) {
      result.put(f.getFilename(), f.getCount());
    }
    return result;
  }
  
  @Override
  public void extractPathsFromFileValue(Set<String> paths, FileValue file) {
    paths.add(file.getPath());
    if(file.getSecondaryFiles()!=null)
      for(FileValue f: file.getSecondaryFiles()) {
        extractPathsFromFileValue(paths, f);
      }
  }
  
  @Override
  public void addOrIncrement(UUID rootId, FileValue file, Integer usage) {
    Set<String> paths = new HashSet<String>();
    extractPathsFromFileValue(paths, file);
    for(String path: paths) {
        intermediaryFilesRepository.increment(rootId, path);
    }
  }
  
  protected Set<String> getUnusedFiles(UUID rootId) {
    List<IntermediaryFileEntity> filesForRootIdList = intermediaryFilesRepository.get(rootId);
    Map<String, Integer> filesForRootId = convertToMap(filesForRootIdList);
    Set<String> unusedFiles = new HashSet<String>();
    for(Iterator<Map.Entry<String, Integer>> it = filesForRootId.entrySet().iterator(); it.hasNext();) {
      Entry<String, Integer> entry = it.next();
      if(entry.getValue() <= 0) {
        unusedFiles.add(entry.getKey());
        intermediaryFilesRepository.delete(rootId, entry.getKey());
        it.remove();
      }
    }
    return unusedFiles;
  }

  @Override
  public void handleInputSent(UUID rootId, Object input) {
    handleInputSent(rootId, input, 1);
  }
  
  @Override
  public void handleInputSent(UUID rootId, Object input, int count) {
    Set<FileValue> files = new HashSet<FileValue>(FileValueHelper.getFilesFromValue(input));
    for(FileValue file: files){
      addOrIncrement(rootId, file, count);
    }
  }

  @Override
  public void handleDanglingOutput(UUID rootId, Object input) {
    Set<String> inputs = new HashSet<String>();
    Set<FileValue> files = new HashSet(FileValueHelper.getFilesFromValue(input));
    for (FileValue file : files) {
      extractPathsFromFileValue(inputs, file);
    }
    decrementFiles(rootId, inputs);
  }
}