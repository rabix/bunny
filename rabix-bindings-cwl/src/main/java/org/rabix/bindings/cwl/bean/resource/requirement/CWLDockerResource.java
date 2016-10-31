package org.rabix.bindings.cwl.bean.resource.requirement;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLDockerResource extends CWLResource {

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
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
