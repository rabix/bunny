package org.rabix.backend.local.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESJobId {

  @JsonProperty("value")
  private String value;

  @JsonCreator
  public TESJobId(@JsonProperty("value") String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "TESJobId [value=" + value + "]";
  }
  
}
