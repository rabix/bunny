package org.rabix.bindings.draft3.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3ExpressionTool extends Draft3JobApp {

  @JsonProperty("expression")
  private Object script;

  public Object getScript() {
    return script;
  }

  @Override
  @JsonIgnore
  public Draft3JobAppType getType() {
    return Draft3JobAppType.EXPRESSION_TOOL;
  }

  @Override
  public String toString() {
    return "Draft3ExpressionTool [script=" + script + ", id=" + id + ", context=" + context + ", description=" + description + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }
  
}
