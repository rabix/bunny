package org.rabix.tes.command.line.service;

public class TESCommandLineException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -341302456035704645L;

  public TESCommandLineException(String message) {
    super(message);
  }
  
  public TESCommandLineException(String message, Throwable t) {
    super(message, t);
  }
  
}
