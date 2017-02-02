package org.rabix.engine.rest.service;

import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;

public interface BackendService {

  <T extends Backend> T create(T backend) throws TransactionException;
  
  void updateHeartbeatInfo(HeartbeatInfo info) throws TransactionException;

  Long getHeartbeatInfo(String id);
  
}
