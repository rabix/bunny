package org.rabix.bindings;

public class BindingWrongVersionException extends BindingException {

  private static final long serialVersionUID = -5493500881508866987L;

  public BindingWrongVersionException(Throwable t) {
    super(t);
  }
  
  public BindingWrongVersionException(String message) {
    super(message);
  }
  
  public BindingWrongVersionException(String message, Throwable t) {
    super(message, t);
  }
  
}
