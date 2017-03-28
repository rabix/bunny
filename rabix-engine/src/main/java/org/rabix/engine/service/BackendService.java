package org.rabix.engine.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;

public interface BackendService {

  <T extends Backend> T create(T backend) throws BackendServiceException;
  
  void startBackend(Backend backend) throws BackendServiceException;
  
  void stopBackend(Backend backend) throws BackendServiceException;
  
  void startInactiveBackend(UUID id) throws BackendServiceException;
  
  void updateHeartbeatInfo(HeartbeatInfo info) throws BackendServiceException;

  void updateHeartbeatInfo(UUID id, Timestamp info) throws BackendServiceException;

  Long getHeartbeatInfo(UUID id);
  
  List<Backend> getActiveBackends();
  
}
