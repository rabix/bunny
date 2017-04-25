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
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.repository.IntermediaryFilesRepository;
import org.rabix.engine.repository.IntermediaryFilesRepository.IntermediaryFileEntity;
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
    List<IntermediaryFileEntity> filesForRootIdList = intermediaryFilesRepository.get(rootId);
    Map<String, Integer> filesForRootId = convertToMap(filesForRootIdList);
    for(String path: checkFiles) {
      if(filesForRootId.containsKey(path)) {
        logger.debug("Decrementing file with path={}", path);
        Integer count = filesForRootId.get(path) - 1;
        filesForRootId.put(path, count);
        intermediaryFilesRepository.update(rootId, path, count);
      }
      else {
        logger.debug("File with path={} not detected", path);
      }
    }
  }
  
  @Override
  public void handleUnusedFiles(Job job){
    fileHandler.handleUnusedFiles(job, getUnusedFiles(job.getRootId()));
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
        Integer count = linkRecordService.findBySourceCount(containerJob.getName(), entry.getKey(), containerJob.getRootId());
        for(FileValue file: files) {
          if(count > 0) {
            addOrIncrement(containerJob.getRootId(), file, count + increment);
          }
        }
      }
    }
  }
  
  public List<LinkRecord> linksForSourcePort(String inputName, List<LinkRecord> links) {
    List<LinkRecord> result = new ArrayList<>();
    for(LinkRecord link: links) {
      if(link.getSourceJobPort().equals(inputName)) {
        result.add(link);
      }
    }
    return result;
  }

  @Override
  public void handleJobCompleted(Job job) {
    if(!job.isRoot()) {
      List<LinkRecord> allLinks = linkRecordService.findBySource(job.getName(), job.getRootId());
      boolean isScatteredOrContainer = false;
      for (Map.Entry<String, Object> entry : job.getOutputs().entrySet()) {
        List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
        if (!files.isEmpty()) {
          List<LinkRecord> links = linksForSourcePort(entry.getKey(), allLinks);
          Integer count = links.size();
          for (LinkRecord link : links) {
            if(link.getDestinationJobId().equals(InternalSchemaHelper.getJobIdFromScatteredId(job.getName())) && (InternalSchemaHelper.getJobNestingDepth(job.getName()) < InternalSchemaHelper.getJobNestingDepth(link.getDestinationJobId()))) {
              isScatteredOrContainer = true;
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
      if(!isScatteredOrContainer) {
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
    for(FileValue f: file.getSecondaryFiles()) {
      extractPathsFromFileValue(paths, f);
    }
  }
  
  @Override
  public void addOrIncrement(UUID rootId, FileValue file, Integer usage) {
    Set<String> paths = new HashSet<String>();
    extractPathsFromFileValue(paths, file);
    List<IntermediaryFileEntity> filesForRootIdList = intermediaryFilesRepository.get(rootId);
    Map<String, Integer> filesForRootId = convertToMap(filesForRootIdList);
    for(String path: paths) {
      if(filesForRootId.containsKey(path)) {
        logger.debug("Increment file usage counter: " + path + ": " + ((Integer) filesForRootId.get(path) + usage));
        filesForRootId.put(path, filesForRootId.get(path) + usage);
        intermediaryFilesRepository.update(rootId, path, filesForRootId.get(path) + usage);
      }
      else {
        logger.debug("Adding file usage counter: " + path + ": " + usage);
        filesForRootId.put(path, usage);
        intermediaryFilesRepository.insert(rootId, path, usage);
      }
    }
  }
  
  protected Set<String> getUnusedFiles(UUID rootId) {
    List<IntermediaryFileEntity> filesForRootIdList = intermediaryFilesRepository.get(rootId);
    Map<String, Integer> filesForRootId = convertToMap(filesForRootIdList);
    Set<String> unusedFiles = new HashSet<String>();
    for(Iterator<Map.Entry<String, Integer>> it = filesForRootId.entrySet().iterator(); it.hasNext();) {
      Entry<String, Integer> entry = it.next();
      if(entry.getValue() == 0) {
        unusedFiles.add(entry.getKey());
        intermediaryFilesRepository.delete(rootId, entry.getKey());
        it.remove();
      }
    }
    return unusedFiles;
  }
  
}