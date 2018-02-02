package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.store.repository.IntermediaryFilesRepository;
import org.rabix.engine.store.repository.IntermediaryFilesRepository.IntermediaryFileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class IntermediaryFilesServiceImpl implements IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(IntermediaryFilesServiceImpl.class);

  private IntermediaryFilesRepository intermediaryFilesRepository;
  private IntermediaryFilesHandler fileHandler;

  @Inject
  protected IntermediaryFilesServiceImpl(IntermediaryFilesHandler handler, IntermediaryFilesRepository intermediaryFilesRepository) {
    this.fileHandler = handler;
    this.intermediaryFilesRepository = intermediaryFilesRepository;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handleUnusedFilesIfAny(Job job) {
    Set<String> unusedFiles = getUnusedFiles(job.getRootId());

    logger.debug("handleUnusedFiles of {}: {}", job.getRootId(), unusedFiles);
    fileHandler.handleUnusedFiles(job, unusedFiles);
  }

  @Override
  public void handleJobFailed(Job job, Job rootJob) {
    handleUnusedFilesIfAny(job);
  }

  @Override
  public void incrementInputFilesReferences(Job job) {
    logger.debug("incrementInputFilesReferences(rootId={}, job={})", job.getRootId(), job.getName());

    Set<FileValue> files = new HashSet<>(FileValueHelper.getFilesFromValue(job.getInputs()));
    for(FileValue file: files){
      addOrIncrement(job.getRootId(), file);
    }
  }

  @Override
  public void decrementInputFilesReferences(Job job) {
    logger.debug("decrementInputFilesReferences(rootId={}, job={})", job.getRootId(), job.getName());

    final UUID rootId = job.getRootId();
    decrement(rootId, job.getInputs());
  }

  @Override
  public void decrementOutputFilesReferences(Job job) {
    logger.debug("decrementOutputFilesReferences(rootId={}, job={}, outputs={})", job.getRootId(), job.getName(), job.getOutputs());

    final UUID rootId = job.getRootId();
    decrement(rootId, job.getOutputs());
  }

  private void decrement(UUID rootId, Map<String, Object> inputOutputMap) {
    if (inputOutputMap == null) {
      return;
    }

    inputOutputMap
            .values()
            .stream()
            .map(FileValueHelper::getFilesFromValue)
            .flatMap(List::stream)
            .forEach(fileValue ->
                    extractPathsFromFileValue(fileValue)
                            .forEach(path -> intermediaryFilesRepository.decrement(rootId, path)));

    logger.debug("State after decrement(rootId={}) : {}", rootId, intermediaryFilesRepository.get(rootId));
  }


  private Map<String, Integer> convertToMap(List<IntermediaryFileEntity> filesForRootId) {
    Map<String, Integer> result = new HashMap<>();
    for(IntermediaryFileEntity f: filesForRootId) {
      result.put(f.getFilename(), f.getCount());
    }
    return result;
  }

  private Set<String> extractPathsFromFileValue(FileValue file) {
    Set<String> paths = new HashSet<>();
    paths.add(file.getPath());

    if (file.getSecondaryFiles() != null) {
      for (FileValue f : file.getSecondaryFiles()) {
        paths.addAll(extractPathsFromFileValue(f));
      }
    }
    return paths;
  }

  private void addOrIncrement(UUID rootId, FileValue file) {
    Set<String> paths = extractPathsFromFileValue(file);
    for(String path: paths) {
        intermediaryFilesRepository.increment(rootId, path);
    }

    logger.debug("State after addOrIncrement(rootId={}) : {}", rootId, intermediaryFilesRepository.get(rootId));
  }

  private Set<String> getUnusedFiles(UUID rootId) {
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
}