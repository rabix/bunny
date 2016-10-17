package org.rabix.bindings.model.requirement;

public class LocalContainerRequirement extends Requirement {

  public LocalContainerRequirement() {
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
    return LOCAL_CONTAINER_REQUIREMENT_TYPE;
  }

  @Override
  public String toString() {
    return "LocalContainerRequirement []";
  }
  
}
