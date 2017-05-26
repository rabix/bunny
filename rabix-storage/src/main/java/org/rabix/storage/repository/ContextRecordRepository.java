package org.rabix.storage.repository;

import java.util.UUID;

import org.rabix.storage.model.ContextRecord;

public interface ContextRecordRepository {

  int insert(ContextRecord contextRecord);
  
  int update(ContextRecord contextRecord);
  
  ContextRecord get(UUID id);

  int delete(UUID id);
  
}
