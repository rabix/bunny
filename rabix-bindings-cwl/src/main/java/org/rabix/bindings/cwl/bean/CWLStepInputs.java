package org.rabix.bindings.cwl.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CWLStepInputs {
  
  @JsonProperty("default")
  protected Object defaultValue;
  
  @JsonProperty("valueFrom")
  protected Object valueFrom;
  
  @JsonCreator
  public CWLStepInputs(@JsonProperty Object defaultValue, @JsonProperty Object valueFrom) {
    this.defaultValue = defaultValue;
    this.valueFrom = valueFrom;
  }
  
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public Object getValueFrom() {
    return valueFrom;
  }
  
  public void setValueFrom(Object valueFrom) {
    this.valueFrom = valueFrom;
  }
  
  public String toString() {
    return "CWLStepInputs [defaultValue=" + defaultValue + ", valueFrom=" + valueFrom + "]";
  }
  
}
