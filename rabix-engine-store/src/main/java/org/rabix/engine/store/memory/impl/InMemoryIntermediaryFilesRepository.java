package org.rabix.engine.store.memory.impl;

import org.rabix.engine.store.repository.IntermediaryFilesRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIntermediaryFilesRepository implements IntermediaryFilesRepository {

  private final Map<UUID, Map<String, IntermediaryFileEntity>> intermediaryFilesRepository;

  public InMemoryIntermediaryFilesRepository() {
    intermediaryFilesRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(UUID rootId, String filename, Integer count) {
    Map<String, IntermediaryFileEntity> intermediaryPerRoot = intermediaryFilesRepository.getOrDefault(rootId, new ConcurrentHashMap<>());
    intermediaryPerRoot.put(filename, new IntermediaryFileEntity(rootId, filename, count));
    intermediaryFilesRepository.put(rootId, intermediaryPerRoot);
  }

  @Override
  public void update(UUID rootId, String filename, Integer count) {
    Map<String, IntermediaryFileEntity> intermediaryPerRoot = intermediaryFilesRepository.get(rootId);
    if (intermediaryPerRoot == null) {
      insert(rootId, filename, count);
    } else {
      IntermediaryFileEntity intermediaryFileEntity = intermediaryPerRoot.get(filename);
      if (intermediaryFileEntity == null) {
        intermediaryFileEntity = new IntermediaryFileEntity(rootId, filename, count);
      } else {
        intermediaryFileEntity.setCount(count);
      }
      intermediaryPerRoot.put(filename, intermediaryFileEntity);
    }
  }

  @Override
  public void delete(UUID rootId, String filename) {
    intermediaryFilesRepository.getOrDefault(rootId, new ConcurrentHashMap<>()).remove(filename);
  }

  @Override
  public void delete(UUID rootId) {
    intermediaryFilesRepository.remove(rootId);
  }

  @Override
  public List<IntermediaryFileEntity> get(UUID rootId) {
    return new ArrayList<>(intermediaryFilesRepository.getOrDefault(rootId, new ConcurrentHashMap<>()).values());
  }

  @Override
  public void deleteByRootIds(Set<UUID> rootIds) {
    for(UUID rootId: rootIds) {
      intermediaryFilesRepository.remove(rootId);
    }
  }

  @Override
  public void decrement(UUID rootId, String filename) {
    Map<String, IntermediaryFileEntity> intermediaryFiles = intermediaryFilesRepository.get(rootId);
    if (intermediaryFiles == null) {
      return;
    }

    IntermediaryFileEntity intermediaryFileEntity = intermediaryFiles.get(filename);
    if (intermediaryFileEntity == null) {
      return;
    }

    intermediaryFileEntity.decrement();
  }

  @Override
  public void increment(UUID rootId, String filename) {
    Map<String, IntermediaryFileEntity> intermediaryFiles = intermediaryFilesRepository.getOrDefault(rootId, new ConcurrentHashMap<>());

    IntermediaryFileEntity intermediaryFileEntity = intermediaryFiles.get(filename);
    if (intermediaryFileEntity == null) {
      intermediaryFileEntity = new IntermediaryFileEntity(rootId, filename, 0);
    }

    intermediaryFileEntity.increment();

    intermediaryFiles.put(filename, intermediaryFileEntity);
    intermediaryFilesRepository.put(rootId, intermediaryFiles);
  }
}
