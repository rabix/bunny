package org.rabix.engine.store.repository;

import org.rabix.engine.store.model.BackendRecord;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BackendRepository {

  void insert(BackendRecord backend);

  BackendRecord get(UUID id);
  
  List<BackendRecord> getByStatus(BackendRecord.Status status);
  
  List<BackendRecord> getAll();
  
  void updateHeartbeatInfo(UUID id, Instant heartbeatInfo);
  
  void updateStatus(UUID id, BackendRecord.Status status);

  Instant getHeartbeatInfo(UUID id);

  void updateConfiguration(UUID id, String backendConfiguration);

}