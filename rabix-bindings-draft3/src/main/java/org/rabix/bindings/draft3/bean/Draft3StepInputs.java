package org.rabix.bindings.draft3.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3StepInputs implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((valueFrom == null) ? 0 : valueFrom.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Draft3StepInputs other = (Draft3StepInputs) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null)
        return false;
    } else if (!defaultValue.equals(other.defaultValue))
      return false;
    if (valueFrom == null) {
      if (other.valueFrom != null)
        return false;
    } else if (!valueFrom.equals(other.valueFrom))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Draft3StepInputs [defaultValue=" + defaultValue + ", valueFrom=" + valueFrom + "]";
  }
  
}