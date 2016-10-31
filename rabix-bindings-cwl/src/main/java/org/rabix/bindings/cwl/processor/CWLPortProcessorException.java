package org.rabix.bindings.cwl.processor;

public class CWLPortProcessorException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1232749409810371749L;

  public CWLPortProcessorException(String message) {
    super(message);
  }
  
  public CWLPortProcessorException(Throwable t) {
    super(t);
  }
  
  public CWLPortProcessorException(String message, Throwable t) {
    super(message, t);
  }
  
}
