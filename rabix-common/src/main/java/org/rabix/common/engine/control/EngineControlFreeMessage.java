package org.rabix.common.engine.control;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EngineControlFreeMessage extends EngineControlMessage {

  @JsonProperty("config")
  private Map<String, Object> config;
  
  @JsonCreator
  public EngineControlFreeMessage(@JsonProperty("config") Map<String, Object> config, @JsonProperty("rootId") String rootId) {
    super(rootId);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
  
  @Override
  @JsonIgnore
  public EngineControlMessageType getType() {
    return EngineControlMessageType.FREE;
  }

}
