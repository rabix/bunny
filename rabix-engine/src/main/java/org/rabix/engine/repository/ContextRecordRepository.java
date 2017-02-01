package org.rabix.engine.repository;

import org.rabix.engine.model.ContextRecord;

import java.util.UUID;

public interface ContextRecordRepository {

  int insert(ContextRecord contextRecord);
  
  int update(ContextRecord contextRecord);
  
  ContextRecord get(UUID id);

  ContextRecord getByExternalId(String externalId);
  
}
