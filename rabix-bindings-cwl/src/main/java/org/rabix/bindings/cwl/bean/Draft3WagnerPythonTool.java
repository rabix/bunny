package org.rabix.bindings.draft3.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3WagnerPythonTool extends Draft3JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public Draft3JobAppType getType() {
    return Draft3JobAppType.WAGNER_PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "Draft3WagnerPythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs()
        + ", getOutputs()=" + getOutputs() + "]";
  }

}
