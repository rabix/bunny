package org.rabix.bindings.sb.bean;

import org.rabix.bindings.model.JobAppType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SBExpressionTool.class)
public class SBExpressionTool extends SBJobApp {

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
    return "SBExpressionTool [script=" + script + ", id=" + getId() + ", context=" + getContext() + ", description=" + getDescription() + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }
  
}
