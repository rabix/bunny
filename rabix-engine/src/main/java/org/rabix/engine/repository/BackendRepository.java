package org.rabix.engine.repository;

import java.sql.Timestamp;
import java.util.List;

import org.rabix.transport.backend.Backend;

public interface BackendRepository {

  public static enum BackendStatus {
    ACTIVE,
    INACTIVE
  }
  
  void insert(String id, Backend backend, Timestamp heartbeatInfo, BackendStatus status);
  
  void update(String id, Backend configuration);
  
  Backend get(String id);
  
  List<Backend> getActive();
  
  void updateHeartbeatInfo(String id, Timestamp heartbeatInfo);
  
  void updateStatus(String id, BackendStatus status);
  
  Timestamp getHeartbeatInfo(String id);
  
}