package org.rabix.backend.local.tes.service;

public class TESServiceException extends Exception {

  private static final long serialVersionUID = -3341213832183821325L;
  
  public TESServiceException(String message) {
    super(message);
  }

  public TESServiceException(String message, Throwable e) {
    super(message, e);
  }

  public TESServiceException(Throwable e) {
    super(e);
  }

}
