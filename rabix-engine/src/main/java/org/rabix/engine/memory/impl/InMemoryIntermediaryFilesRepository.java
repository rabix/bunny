package org.rabix.engine.memory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.engine.repository.IntermediaryFilesRepository;

public class InMemoryIntermediaryFilesRepository implements IntermediaryFilesRepository {

  Map<UUID, List<IntermediaryFileEntity>> intermediaryFilesRepository;
  
  public InMemoryIntermediaryFilesRepository() {
    intermediaryFilesRepository = new ConcurrentHashMap<UUID, List<IntermediaryFileEntity>>();
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

}
