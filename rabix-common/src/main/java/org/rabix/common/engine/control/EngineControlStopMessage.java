package org.rabix.common.engine.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EngineControlStopMessage extends EngineControlMessage {
  
  @JsonProperty("id")
  private String id;

  @JsonCreator
  public EngineControlStopMessage(@JsonProperty("id") String id, @JsonProperty("rootId") String rootId) {
    super(rootId);
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public EngineControlMessageType getType() {
    return EngineControlMessageType.STOP;
  }

}

