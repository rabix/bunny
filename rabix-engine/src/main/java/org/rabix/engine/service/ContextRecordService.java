package org.rabix.engine.service;

import org.rabix.engine.SchemaHelper;
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
  
  public synchronized ContextRecord find(String id) {
    return contextRecordRepository.get(SchemaHelper.toUUID(id));
  }

  public synchronized void delete(String id) {
    contextRecordRepository.delete(SchemaHelper.toUUID(id));
  }
  
}
