package org.rabix.bindings.draft3.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3PythonTool extends Draft3JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public Draft3JobAppType getType() {
    return Draft3JobAppType.PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "Draft3PythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs() + ", getOutputs()=" + getOutputs() + "]";
  }

}
