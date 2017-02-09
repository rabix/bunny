package org.rabix.engine.repository;

import org.rabix.engine.model.ContextRecord;

public interface ContextRecordRepository {

  int insert(ContextRecord contextRecord);
  
  int update(ContextRecord contextRecord);
  
  ContextRecord get(String id);
  
}
