package org.rabix.bindings.sb.bean.resource.requirement;

import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBDockerResource extends SBResource {

  public static String KEY_DOCKER_PULL = "dockerPull";
  public static String KEY_DOCKER_IMAGE_ID = "dockerImageId";

  @JsonIgnore
  public String getDockerPull() {
    return getValue(KEY_DOCKER_PULL);
  }

  @JsonIgnore
  public String getImageId() {
    return getValue(KEY_DOCKER_IMAGE_ID);
  }

  @Override
  @JsonIgnore
  public SBResourceType getTypeEnum() {
    return SBResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
