package org.rabix.engine.memory.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.repository.ContextRecordRepository;

import com.google.inject.Inject;

public class InMemoryContextRecordRepository implements ContextRecordRepository {

  Map<UUID, ContextRecord> contextRecordRepository;
  
  @Inject
  public InMemoryContextRecordRepository() {
    this.contextRecordRepository = new ConcurrentHashMap<UUID, ContextRecord>();
  }

  @Override
  public synchronized int insert(ContextRecord contextRecord) {
    contextRecordRepository.put(contextRecord.getId(), contextRecord);
    return 1;
  }

  @Override
  public synchronized int update(ContextRecord contextRecord) {
    contextRecordRepository.put(contextRecord.getId(), contextRecord);
    return 1;
  }

  @Override
  public synchronized ContextRecord get(UUID id) {
    return contextRecordRepository.get(id);
  }

  @Override
  public synchronized int delete(UUID id) {
    contextRecordRepository.remove(id);
    return 1;
  }

}
