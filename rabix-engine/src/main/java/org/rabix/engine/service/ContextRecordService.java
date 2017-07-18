package org.rabix.engine.service;

import java.util.UUID;

import org.rabix.engine.store.model.ContextRecord;

public interface ContextRecordService {

  void create(ContextRecord contextRecord);
  
  void update(ContextRecord context);
  
  ContextRecord find(UUID id);

  void delete(UUID id);

}
