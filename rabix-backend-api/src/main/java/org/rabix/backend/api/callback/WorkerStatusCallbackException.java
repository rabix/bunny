package org.rabix.backend.api.callback;

public class WorkerStatusCallbackException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -1414465503966795287L;

  public WorkerStatusCallbackException(Throwable t) {
    super(t);
  }
  
  public WorkerStatusCallbackException(String message) {
    super(message);
  }

  public WorkerStatusCallbackException(String message, Throwable t) {
    super(message, t);
  }

}
