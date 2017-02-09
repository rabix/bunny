package org.rabix.common.engine.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = EngineControlStopMessage.class, name = "STOP"),
    @Type(value = EngineControlFreeMessage.class, name = "FREE")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EngineControlMessage {

  public static enum EngineControlMessageType {
    STOP, FREE
  }
  
  @JsonProperty("rootId")
  protected String rootId;

  @JsonCreator
  public EngineControlMessage(@JsonProperty("rootId") String rootId) {
    this.rootId = rootId;
  }
  
  public String getRootId() {
    return rootId;
  }
  
  public abstract EngineControlMessageType getType();
  
}
