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
import java.util.function.Consumer;

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
  public void registerOutputFiles(UUID rootId, Object value) {
    FileValueHelper
            .getFilesFromValue(value)
            .forEach(fileValue ->
                    extractPathsFromFileValue(fileValue)
                            .forEach(path -> intermediaryFilesRepository.insertIfNotExists(rootId, path, 0)));
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
  public void incrementInputFilesReferences(UUID rootId, Object value) {
    logger.debug("incrementInputFilesReferences(rootId={}, value={})", rootId, value);
    FileValueHelper.getFilesFromValue(value).forEach(fileValue -> addOrIncrement(rootId, fileValue));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void decrementInputFilesReferences(UUID rootId, Object value) {
    logger.debug("decrementInputFilesReferences(rootId={}, value={})", rootId, value);
    decrement(rootId, (Map<String, Object>) value);
  }

  private void decrement(UUID rootId, Map<String, Object> inputOutputMap) {
    if (inputOutputMap == null) {
      return;
    }

    forEachPath(inputOutputMap, path -> intermediaryFilesRepository.decrement(rootId, path));
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

    String path = file.getPath();
    if (path != null) {
      paths.add(path);
    }

    if (file.getSecondaryFiles() != null) {
      for (FileValue f : file.getSecondaryFiles()) {
        paths.addAll(extractPathsFromFileValue(f));
      }
    }
    return paths;
  }

  private void addOrIncrement(UUID rootId, FileValue file) {
    if (file == null || file.getPath() == null) {
      return;
    }

    Set<String> paths = extractPathsFromFileValue(file);
    for(String path: paths) {
        intermediaryFilesRepository.increment(rootId, path);
    }
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

  private void forEachPath(Map<String, Object> map, Consumer<String> consumer) {
    map.values().stream()
            .map(FileValueHelper::getFilesFromValue)
            .flatMap(List::stream)
            .forEach(fileValue ->
                    extractPathsFromFileValue(fileValue)
                            .forEach(consumer));
  }
}