package org.rabix.bindings.cwl.processor;

public class CWLPortProcessorResult {

  private Object value;
  private boolean processed;
  
  public CWLPortProcessorResult(Object value, boolean processed) {
    this.value = value;
    this.processed = processed;
  }

  public Object getValue() {
    return value;
  }

  public boolean isProcessed() {
    return processed;
  }

  @Override
  public String toString() {
    return "PortProcessorResult [value=" + value + ", processed=" + processed + "]";
  }
  
}
