package org.rabix.bindings.draft3.expression;

public class Draft3ExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public Draft3ExpressionException(String message) {
    super(message);
  }
  
  public Draft3ExpressionException(Throwable e) {
    super(e);
  }

  public Draft3ExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
