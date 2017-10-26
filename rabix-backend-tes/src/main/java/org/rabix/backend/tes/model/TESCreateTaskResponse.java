package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESCreateTaskResponse {

  @JsonProperty("id")
  private String id;

  @JsonCreator
  public TESCreateTaskResponse(@JsonProperty("id") String value) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "TESCreateTaskResponse [id=" + id + "]";
  }
  
}
