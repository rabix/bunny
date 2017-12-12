package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.repository.ContextRecordRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryContextRecordRepository implements ContextRecordRepository {

  private final Map<UUID, ContextRecord> contextRecordRepository;

  @Inject
  public InMemoryContextRecordRepository() {
    this.contextRecordRepository = new ConcurrentHashMap<>();
  }

  @Override
  public int insert(ContextRecord contextRecord) {
    contextRecordRepository.put(contextRecord.getId(), contextRecord);
    return 1;
  }

  @Override
  public int update(ContextRecord contextRecord) {
    contextRecordRepository.put(contextRecord.getId(), contextRecord);
    return 1;
  }

  @Override
  public ContextRecord get(UUID id) {
    return contextRecordRepository.get(id);
  }

  @Override
  public int delete(UUID id) {
    contextRecordRepository.remove(id);
    return 1;
  }

}
