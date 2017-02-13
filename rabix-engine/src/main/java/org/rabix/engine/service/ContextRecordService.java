package org.rabix.engine.service;

import java.util.UUID;

import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.repository.ContextRecordRepository;

import com.google.inject.Inject;

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

  public synchronized void delete(UUID id) {
    contextRecordRepository.delete(id);
  }
  
}
