package org.rabix.engine.service;

public class BackendServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 3377465733224604020L;

  public BackendServiceException(String message) {
    super(message);
  }
  
  public BackendServiceException(Throwable t) {
    super(t);
  }
  
}
