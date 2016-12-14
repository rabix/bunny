package org.rabix.backend.local.tes.client;

public class TESHTTPClientException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -781768089842055413L;
  
  public TESHTTPClientException(Throwable t) {
    super(t);
  }
  
  public TESHTTPClientException(String message) {
    super(message);
  }
  
  public TESHTTPClientException(String message, Throwable t) {
    super(message, t);
  }

}
