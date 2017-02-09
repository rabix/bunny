package org.rabix.engine.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.rabix.transport.backend.Backend;

public interface BackendRepository {

  public static enum BackendStatus {
    ACTIVE,
    INACTIVE
  }
  
  void insert(UUID id, Backend backend, Timestamp heartbeatInfo, BackendStatus status);
  
  void update(UUID id, Backend configuration);
  
  Backend get(UUID id);
  
  List<Backend> getByStatus(BackendStatus status);
  
  void updateHeartbeatInfo(UUID id, Timestamp heartbeatInfo);
  
  void updateStatus(UUID id, BackendStatus status);
  
  Timestamp getHeartbeatInfo(UUID id);

}