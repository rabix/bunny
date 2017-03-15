package org.rabix.engine.service;

public interface BootstrapService {

  void replay() throws BootstrapServiceException;
  
}
