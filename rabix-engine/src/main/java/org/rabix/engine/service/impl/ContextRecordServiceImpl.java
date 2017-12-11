package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.store.lru.context.ContextRecordCache;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.repository.ContextRecordRepository;

import java.util.UUID;

public class ContextRecordServiceImpl implements ContextRecordService {

  private ContextRecordRepository contextRecordRepository;
  private ContextRecordCache contextRecordCache;

  @Inject
  public ContextRecordServiceImpl(ContextRecordRepository contextRecordRepository, ContextRecordCache contextRecordCache) {
    this.contextRecordRepository = contextRecordRepository;
    this.contextRecordCache = contextRecordCache;
  }

  public void create(ContextRecord contextRecord) {
    contextRecordRepository.insert(contextRecord);
    contextRecordCache.put(contextRecord.getId(), contextRecord);
  }

  public void update(ContextRecord context) {
    contextRecordRepository.update(context);
    contextRecordCache.put(context.getId(), context);
  }

  public ContextRecord find(UUID id) {
    ContextRecord contextRecord = contextRecordCache.get(id);
    if (contextRecord == null) {
      contextRecord = contextRecordRepository.get(id);
    }

    if (contextRecord != null) {
      contextRecordCache.put(id, contextRecord);
    }

    return contextRecord;
  }

  public void delete(UUID id) {
    contextRecordRepository.delete(id);
    contextRecordCache.remove(id);
  }
}
