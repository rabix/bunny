package org.rabix.bindings.cwl.service;

public class CWLGlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public CWLGlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public CWLGlobException(Throwable t) {
    super(t);
  }
}
