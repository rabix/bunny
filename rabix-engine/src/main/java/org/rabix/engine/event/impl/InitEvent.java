package org.rabix.engine.event.impl;

import java.util.Map;

import org.rabix.engine.event.Event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is a starter event. It triggers the algorithm start. 
 */
public class InitEvent implements Event {

  @JsonProperty("eventGroupId")
  private final String eventGroupId;
  @JsonProperty("value")
  private final Map<String, Object> value;
  @JsonProperty("rootId")
  private final String rootId;
  @JsonProperty("config")
  private final Map<String, Object> config;
  @JsonProperty("dagHash")
  private final String dagHash;
  
  @JsonCreator
  public InitEvent(@JsonProperty("eventGroupId") String eventGroupId, @JsonProperty("value") Map<String, Object> value,
      @JsonProperty("rootId") String rootId, @JsonProperty("config") Map<String, Object> config, @JsonProperty("dagHash") String dagHash) {
    super();
    this.eventGroupId = eventGroupId;
    this.value = value;
    this.rootId = rootId;
    this.config = config;
    this.dagHash = dagHash;
  }

  public Map<String, Object> getValue() {
    return value;
  }
  
  public String getRootId() {
    return rootId;
  }
  
  @Override
  public String getContextId() {
    return rootId;
  }
  
  @Override
  public String getEventGroupId() {
    return eventGroupId;
  }
  
  public Map<String, Object> getConfig() {
    return config;
  }
  
  public String getDagHash() {
    return dagHash;
  }

  @Override
  public EventType getType() {
    return EventType.INIT;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((config == null) ? 0 : config.hashCode());
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    InitEvent other = (InitEvent) obj;
    if (config == null) {
      if (other.config != null)
        return false;
    } else if (!config.equals(other.config))
      return false;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "InitEvent [value=" + value + ", rootId=" + rootId + ", config=" + config + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return PersistentEventType.INIT;
  }

}
