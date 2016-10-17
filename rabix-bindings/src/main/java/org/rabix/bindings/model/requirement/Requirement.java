package org.rabix.bindings.model.requirement;

public abstract class Requirement {

  static String FILE_REQUIREMENT_TYPE = "fileRequirement";
  static String DOCKER_REQUIREMENT_TYPE = "dockerRequirement";
  static String LOCAL_CONTAINER_REQUIREMENT_TYPE = "localContainerRequirement";
  static String RESOURCE_REQUIREMENT_TYPE = "resourceRequirement";
  static String ENVIRONMENT_VARIABLE_REQUIREMENT_TYPE = "environmentVariableRequirement";

  public abstract boolean isCustom();
  
  public abstract Object getData();
    
  public abstract String getType();
  
  
}
