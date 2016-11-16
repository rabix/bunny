package org.rabix.bindings.draft3.bean.resource.requirement;

import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft3DockerResource extends Draft3Resource {

  public static String KEY_DOCKER_PULL = "dockerPull";
  public static String KEY_DOCKER_IMAGE_ID = "dockerImageId";
  public static String KEY_DOCKER_OUTPUT_DIR = "dockerOutputDirectory";

  @JsonIgnore
  public String getDockerPull() {
    return getValue(KEY_DOCKER_PULL);
  }

  @JsonIgnore
  public String getImageId() {
    return getValue(KEY_DOCKER_IMAGE_ID);
  }

  public String getDockerOutputDirectory() {
    return getValue(KEY_DOCKER_OUTPUT_DIR);
  }
  
  @Override
  @JsonIgnore
  public Draft3ResourceType getTypeEnum() {
    return Draft3ResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
