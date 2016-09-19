package org.rabix.bindings.cwl.service;

public class Draft3GlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public Draft3GlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public Draft3GlobException(Throwable t) {
    super(t);
  }
}
