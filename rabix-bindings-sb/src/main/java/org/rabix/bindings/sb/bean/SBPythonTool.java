package org.rabix.bindings.sb.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SBPythonTool.class)
public class SBPythonTool extends SBJobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public SBJobAppType getType() {
    return SBJobAppType.PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "SBPythonTool [function=" + function + ", id=" + getId() + ", getInputs()=" + getInputs() + ", getOutputs()=" + getOutputs() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SBPythonTool other = (SBPythonTool) obj;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    return true;
  }
}
