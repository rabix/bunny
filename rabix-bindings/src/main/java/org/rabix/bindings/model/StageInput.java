package org.rabix.bindings.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;

public enum StageInput {
  COPY("copy"), LINK("link");
  
  private String value;

  @JsonValue
  public String getValue() {
    return value;
  }
  private StageInput(String value) {
    this.value = value;
  }
  
  public static StageInput get(String value) {
    Preconditions.checkNotNull(value);
    for (StageInput stageInput : values()) {
      if (value.compareToIgnoreCase(stageInput.value) == 0) {
        return stageInput;
      }
    }
    throw new IllegalArgumentException("Wrong stageInput value " + value);
  }
}