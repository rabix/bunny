package org.rabix.bindings.draft3;

public class Draft3Exception extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public Draft3Exception(String message) {
    super(message);
  }
  
  public Draft3Exception(String message, Throwable t) {
    super(message, t);
  }
}
