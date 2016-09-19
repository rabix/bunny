package org.rabix.bindings.cwl.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CWLWagnerPythonTool extends CWLJobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public CWLJobAppType getType() {
    return CWLJobAppType.WAGNER_PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "CWLWagnerPythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs()
        + ", getOutputs()=" + getOutputs() + "]";
  }

}
