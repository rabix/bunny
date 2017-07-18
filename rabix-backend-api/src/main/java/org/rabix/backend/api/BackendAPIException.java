package org.rabix.backend.api;

public class BackendAPIException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 5200615808201208183L;
  
  public BackendAPIException(String message) {
    super(message);
  }

  public BackendAPIException(String message, Throwable t) {
    super(message, t);
  }
  
  public BackendAPIException(Throwable t) {
    super(t);
  }
  
}
