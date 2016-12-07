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

}
