package org.rabix.engine.repository;

import java.util.UUID;

import org.rabix.engine.model.ContextRecord;

public interface ContextRecordRepository {

  int insert(ContextRecord contextRecord);
  
  int update(ContextRecord contextRecord);
  
  ContextRecord get(UUID id);
  
}
