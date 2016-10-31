package org.rabix.bindings.sb.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    return "SBPythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs() + ", getOutputs()=" + getOutputs() + "]";
  }

}
