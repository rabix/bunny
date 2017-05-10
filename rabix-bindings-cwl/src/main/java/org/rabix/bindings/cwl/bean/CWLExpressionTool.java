package org.rabix.bindings.cwl.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(as = CWLExpressionTool.class)
public class CWLExpressionTool extends CWLJobApp {

  @JsonProperty("expression")
  private Object script;

  public Object getScript() {
    return script;
  }

  @Override
  @JsonIgnore
  public CWLJobAppType getType() {
    return CWLJobAppType.EXPRESSION_TOOL;
  }

  @Override
  public String toString() {
    return "CWLExpressionTool [script=" + script + ", id=" + getId() + ", context=" + getContext() + ", description=" + getDescription() + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

  @Override
  public List<String> validate() {
    List<String> validationErrors = new ArrayList<>();
    validationErrors.addAll(validatePortUniqueness());
    return validationErrors;
  }
}
