package org.rabix.engine.service;

import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.repository.ContextRecordRepository;

import com.google.inject.Inject;

import java.util.UUID;

public class ContextRecordService {

  private ContextRecordRepository contextRecordRepository;
  
  @Inject
  public ContextRecordService(ContextRecordRepository contextRecordRepository) {
    this.contextRecordRepository = contextRecordRepository;
  }
  
  public synchronized void create(ContextRecord contextRecord) {
    contextRecordRepository.insert(contextRecord);
  }
  
  public synchronized void update(ContextRecord context) {
    contextRecordRepository.update(context);
  }
  
  public synchronized ContextRecord find(UUID id) {
    return contextRecordRepository.get(id);
  }

  public synchronized ContextRecord findByExternalId(String externalId) {
    return contextRecordRepository.getByExternalId(externalId);
  }
  
}
