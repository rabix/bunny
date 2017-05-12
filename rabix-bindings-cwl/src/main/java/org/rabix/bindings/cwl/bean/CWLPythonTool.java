package org.rabix.bindings.cwl.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.rabix.bindings.model.ValidationReport;

@JsonDeserialize(as = CWLPythonTool.class)
public class CWLPythonTool extends CWLJobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public CWLJobAppType getType() {
    return CWLJobAppType.PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "CWLPythonTool [function=" + function + ", id=" + getId() + ", getInputs()=" + getInputs()
        + ", getOutputs()=" + getOutputs() + "]";
  }

  @Override
  public ValidationReport validate() {
    return new ValidationReport();
  }
}
