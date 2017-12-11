package org.rabix.engine.store.memory.impl;

import org.rabix.engine.store.repository.IntermediaryFilesRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIntermediaryFilesRepository implements IntermediaryFilesRepository {

  private final Map<UUID, List<IntermediaryFileEntity>> intermediaryFilesRepository;

  public InMemoryIntermediaryFilesRepository() {
    intermediaryFilesRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(UUID rootId, String filename, Integer count) {

    if(intermediaryFilesRepository.containsKey(rootId)) {
      List<IntermediaryFileEntity> intermediaryPerRoot = intermediaryFilesRepository.get(rootId);
      intermediaryPerRoot.add(new IntermediaryFileEntity(rootId, filename, count));
    }
    else {
      List<IntermediaryFileEntity> intermediaryPerRoot = new ArrayList<>();
      intermediaryPerRoot.add(new IntermediaryFileEntity(rootId, filename, count));
      intermediaryFilesRepository.put(rootId, intermediaryPerRoot);
    }
  }

  @Override
  public void update(UUID rootId, String filename, Integer count) {
    if(intermediaryFilesRepository.containsKey(rootId)) {
      List<IntermediaryFileEntity> intermediaryPerRoot = intermediaryFilesRepository.get(rootId);
      for(IntermediaryFileEntity file: intermediaryPerRoot) {
        if(file.getFilename().equals(filename)) {
          file.setCount(count);
          break;
        }
      }
    }
  }

  @Override
  public void delete(UUID rootId, String filename) {
    if(intermediaryFilesRepository.containsKey(rootId)) {
      List<IntermediaryFileEntity> intermediaryPerRoot = intermediaryFilesRepository.get(rootId);
      for (Iterator<IntermediaryFileEntity> iterator = intermediaryPerRoot.iterator(); iterator.hasNext();) {
        IntermediaryFileEntity file = iterator.next();
        if (file.getFilename().equals(filename)) {
            iterator.remove();
            break;
        }
      }
    }
  }

  @Override
  public void delete(UUID rootId) {
    if(intermediaryFilesRepository.containsKey(rootId)) {
      intermediaryFilesRepository.remove(rootId);
    }
  }

  @Override
  public List<IntermediaryFileEntity> get(UUID rootId) {
    if(intermediaryFilesRepository.containsKey(rootId)) {
      return intermediaryFilesRepository.get(rootId);
    }
    return Collections.emptyList();
  }

  @Override
  public void deleteByRootIds(Set<UUID> rootIds) {
    for(UUID rootId: rootIds) {
      intermediaryFilesRepository.remove(rootId);
    }
  }

  @Override
  public void decrement(UUID rootId, String filename) {

  }
  @Override
  public void increment(UUID rootId, String filename) {

  }
}
