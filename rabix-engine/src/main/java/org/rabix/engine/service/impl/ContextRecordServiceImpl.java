package org.rabix.engine.service.impl;

import java.util.UUID;

import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.service.ContextRecordService;

import com.google.inject.Inject;

public class ContextRecordServiceImpl implements ContextRecordService {

  private ContextRecordRepository contextRecordRepository;
  
  @Inject
  public ContextRecordServiceImpl(ContextRecordRepository contextRecordRepository) {
    this.contextRecordRepository = contextRecordRepository;
  }
  
  public void create(ContextRecord contextRecord) {
    contextRecordRepository.insert(contextRecord);
  }
  
  public void update(ContextRecord context) {
    contextRecordRepository.update(context);
  }
  
  public ContextRecord find(UUID id) {
    return contextRecordRepository.get(id);
  }

  public void delete(UUID id) {
    contextRecordRepository.delete(id);
  }
  
}
