package org.rabix.common.engine.control;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EngineControlStopMessage extends EngineControlMessage {
  
  @JsonProperty("id")
  private UUID id;

  @JsonCreator
  public EngineControlStopMessage(@JsonProperty("id") UUID id, @JsonProperty("rootId") UUID rootId) {
    super(rootId);
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  @Override
  public EngineControlMessageType getType() {
    return EngineControlMessageType.STOP;
  }

}

