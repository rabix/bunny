package org.rabix.backend.api.callback;

public class ExecutorStatusCallbackException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -1414465503966795287L;

  public ExecutorStatusCallbackException(Throwable t) {
    super(t);
  }
  
  public ExecutorStatusCallbackException(String message) {
    super(message);
  }

  public ExecutorStatusCallbackException(String message, Throwable t) {
    super(message, t);
  }

}
