package org.rabix.engine.service;

public interface BootstrapService {

  void start() throws BootstrapServiceException;
  
  void replay() throws BootstrapServiceException;

}
