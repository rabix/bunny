package org.rabix.engine.repository;

import java.sql.Timestamp;

import org.rabix.transport.backend.Backend;

public interface BackendRepository {

  void insert(String id, Backend backend);
  
  void update(String id, Backend configuration);
  
  Backend get(String id);
  
  void updateHeartbeatInfo(String id, Timestamp heartbeatInfo);
  
  Timestamp getHeartbeatInfo(String id);
  
}