package org.rabix.engine.store.repository;

import java.util.UUID;

import org.rabix.engine.store.model.ContextRecord;

public interface ContextRecordRepository {

  int insert(ContextRecord contextRecord);
  
  int update(ContextRecord contextRecord);
  
  ContextRecord get(UUID id);

  int delete(UUID id);
  
}
