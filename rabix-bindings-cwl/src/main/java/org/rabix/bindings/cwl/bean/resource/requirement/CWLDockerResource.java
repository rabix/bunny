package org.rabix.bindings.cwl.bean.resource.requirement;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLDockerResource extends CWLResource {

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
  
  @JsonIgnore
  public String getDockerOutputDirectory() {
    return getValue(KEY_DOCKER_OUTPUT_DIR);
  }

  @Override
  @JsonIgnore
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
