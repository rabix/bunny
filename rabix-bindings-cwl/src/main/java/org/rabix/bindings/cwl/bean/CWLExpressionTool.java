package org.rabix.bindings.cwl.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.rabix.bindings.model.ValidationReport;

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
  public ValidationReport validate() {
    List<String> errors = new ArrayList<>();
    List<String> warnigs = new ArrayList<>();
    errors.addAll(validatePortUniqueness());
    if (script == null) {
      errors.add("ExpressionTool must have an expression");
    } else if (!(script instanceof String)) {
      errors.add("ExpressionTool expression must be a string, got '" + script + "' instead");
    } else if (((String) script).isEmpty()) {
      warnigs.add("Expression is empty");
    }
    return new ValidationReport(errors, warnigs);
  }
}
