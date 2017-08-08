package org.rabix.bindings.draft3.bean;

import org.rabix.bindings.model.JobAppType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Draft3PythonTool.class)
public class Draft3PythonTool extends Draft3JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public JobAppType getType() {
    return JobAppType.PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "Draft3PythonTool [function=" + function + ", id=" + getId() + ", getInputs()=" + getInputs() + ", getOutputs()=" + getOutputs() + "]";
  }

}
