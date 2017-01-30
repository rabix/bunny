package org.rabix.engine.rest.service;

import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.transport.backend.Backend;

public interface BackendService {

  <T extends Backend> T create(T backend) throws TransactionException;
  
}
