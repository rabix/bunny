package org.rabix.storage.repository;

import org.rabix.storage.model.BackendRecord;

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

  void updateConfiguration(UUID id, Map<String, ?> backendConfiguration);

  Instant getHeartbeatInfo(UUID id);

}