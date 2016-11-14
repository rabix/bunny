package org.rabix.bindings.model.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DockerContainerRequirement extends Requirement {

  @JsonProperty("dockerPull")
  private final String dockerPull;
  @JsonProperty("dockerImageId")
  private final String dockerImageId;
  @JsonProperty("dockerOutputDirectory")
  private final String dockerOutputDirectory;
  
  @JsonCreator
  public DockerContainerRequirement(String dockerPull, String dockerImageId, String dockerOutputDirectory) {
    this.dockerPull = dockerPull;
    this.dockerImageId = dockerImageId;
    this.dockerOutputDirectory = dockerOutputDirectory;
  }

  public String getDockerPull() {
    return dockerPull;
  }

  public String getDockerImageId() {
    return dockerImageId;
  }
  
  public String getDockerOutputDirectory() {
    return dockerOutputDirectory;
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
    return DOCKER_REQUIREMENT_TYPE;
  }
  
  @Override
  public String toString() {
    return "DockerContainerRequirement [dockerPull=" + dockerPull + ", dockerImageId=" + dockerImageId + "]";
  }
}
