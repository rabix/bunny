package org.rabix.engine.repository;

import java.sql.Timestamp;
import java.util.List;

import org.rabix.transport.backend.Backend;

import java.util.UUID;

public interface BackendRepository {


  void insert(Backend backend);

  public static enum BackendStatus {
    ACTIVE,
    INACTIVE
  }
  
  void insert(Backend backend, Timestamp heartbeatInfo, BackendStatus status);
  

  void update(Backend configuration);

  Backend get(UUID id);

  Backend getByName(String name);
  
  List<Backend> getByStatus(BackendStatus status);
  
  void updateHeartbeatInfo(UUID id, Timestamp heartbeatInfo);
  
  void updateStatus(UUID id, BackendStatus status);
  
  Timestamp getHeartbeatInfo(UUID id);

}