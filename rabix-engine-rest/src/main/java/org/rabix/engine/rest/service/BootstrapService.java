package org.rabix.engine.rest.service;

public interface BootstrapService {

  void replay() throws BootstrapServiceException;
  
}
