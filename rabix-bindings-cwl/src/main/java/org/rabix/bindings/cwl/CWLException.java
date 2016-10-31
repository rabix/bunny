package org.rabix.bindings.cwl;

public class CWLException extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public CWLException(String message) {
    super(message);
  }
  
  public CWLException(String message, Throwable t) {
    super(message, t);
  }
}
