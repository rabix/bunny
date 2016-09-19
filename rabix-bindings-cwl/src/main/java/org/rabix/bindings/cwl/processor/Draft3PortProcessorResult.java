package org.rabix.bindings.draft3.processor;

public class Draft3PortProcessorResult {

  private Object value;
  private boolean processed;
  
  public Draft3PortProcessorResult(Object value, boolean processed) {
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
