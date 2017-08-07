package org.rabix.bindings.draft2.bean;

import org.rabix.bindings.model.JobAppType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Draft2ExpressionTool.class)
public class Draft2ExpressionTool extends Draft2JobApp {

  @JsonProperty("expression")
  private Object script;

  public Object getScript() {
    return script;
  }

  @Override
  @JsonIgnore
  public JobAppType getType() {
    return JobAppType.EXPRESSION_TOOL;
  }

  @Override
  public String toString() {
    return "Draft2ExpressionTool [script=" + script + ", id=" + getId() + ", context=" + getContext() + ", description=" + getDescription() + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }
  
}
