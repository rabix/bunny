package org.rabix.bindings.model.requirement;

import java.util.Map;

public class EnvironmentVariableRequirement extends Requirement  {

  private final Map<String, String> variables;
  
  public EnvironmentVariableRequirement(Map<String, String> variables) {
    this.variables = variables;
  }
  
  public Map<String, String> getVariables() {
    return variables;
  }

  @Override
  public String toString() {
    return "EnvironmentVariableRequirement [variables=" + variables + "]";
  }

  @Override
  public boolean isCustom() {
    return false;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getType() {
    return ENVIRONMENT_VARIABLE_REQUIREMENT_TYPE;
  }
  
}
