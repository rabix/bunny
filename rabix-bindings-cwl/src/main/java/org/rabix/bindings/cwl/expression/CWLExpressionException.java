package org.rabix.bindings.cwl.expression;

public class CWLExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public CWLExpressionException(String message) {
    super(message);
  }
  
  public CWLExpressionException(Throwable e) {
    super(e);
  }

  public CWLExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
