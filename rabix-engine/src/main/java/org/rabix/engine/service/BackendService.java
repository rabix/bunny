package org.rabix.engine.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;

public interface BackendService {

  static String BACKEND_TYPES_KEY = "backend.embedded.types";
  
  void scanEmbedded();
  
  boolean isEnabled(String type);
  
  <T extends Backend> T create(T backend) throws BackendServiceException;
    
  void stopBackend(Backend backend) throws BackendServiceException;
  
  void startBackend(Backend backend) throws BackendServiceException;
  
  void updateHeartbeatInfo(HeartbeatInfo info) throws BackendServiceException;

  void updateHeartbeatInfo(UUID id, Instant info) throws BackendServiceException;

  Long getHeartbeatInfo(UUID id);
  
  List<Backend> getActiveBackends();
  
  List<Backend> getActiveRemoteBackends();
  
  List<Backend> getAllBackends();

}
