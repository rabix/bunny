package org.rabix.bindings.cwl.expression;

public class CWLExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public CWLExpressionTimeoutException(String message) {
    super(message);
  }
  
  public CWLExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
