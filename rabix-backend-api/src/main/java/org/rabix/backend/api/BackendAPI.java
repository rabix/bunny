package org.rabix.backend.api;

import org.rabix.transport.backend.Backend;

public interface BackendAPI {

  Backend start() throws BackendAPIException;
  
  void initialize(Backend backend) throws BackendAPIException;
  
}
