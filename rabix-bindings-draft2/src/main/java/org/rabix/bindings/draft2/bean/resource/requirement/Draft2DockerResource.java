package org.rabix.bindings.draft2.bean.resource.requirement;

import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2DockerResource extends Draft2Resource {

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
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
