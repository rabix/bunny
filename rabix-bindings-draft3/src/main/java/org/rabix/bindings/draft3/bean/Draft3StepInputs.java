package org.rabix.bindings.draft3.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3StepInputs {
  
  @JsonProperty("default")
  protected Object defaultValue;
  @JsonProperty("valueFrom")
  protected Object valueFrom;
  
  @JsonCreator
  public Draft3StepInputs(@JsonProperty Object defaultValue, @JsonProperty Object valueFrom) {
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

  @Override
  public String toString() {
    return "Draft3StepInputs [defaultValue=" + defaultValue + ", valueFrom=" + valueFrom + "]";
  }
  
}