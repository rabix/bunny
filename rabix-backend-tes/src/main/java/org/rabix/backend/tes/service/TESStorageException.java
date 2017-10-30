package org.rabix.backend.tes.service;

public class TESStorageException extends Exception {

  private static final long serialVersionUID = -3341213832183821325L;
  
  public TESStorageException(String message) {
    super(message);
  }

  public TESStorageException(String message, Throwable e) {
    super(message, e);
  }

  public TESStorageException(Throwable e) {
    super(e);
  }

}
