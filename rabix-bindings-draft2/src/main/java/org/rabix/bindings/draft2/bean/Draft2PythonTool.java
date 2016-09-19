package org.rabix.bindings.draft2.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft2PythonTool extends Draft2JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public Draft2JobAppType getType() {
    return Draft2JobAppType.PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "Draft2PythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs() + ", getOutputs()=" + getOutputs() + "]";
  }

}
