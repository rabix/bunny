package org.rabix.engine.event.impl;

import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.event.Event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is a starter event. It triggers the algorithm start. 
 */
public class InitEvent implements Event {

  @JsonProperty("eventGroupId")
  private final UUID eventGroupId;
  @JsonProperty("node")
  private final DAGNode node;
  @JsonProperty("value")
  private final Map<String, Object> value;

  @JsonProperty("rootId")
  private final UUID rootId;

  @JsonProperty("config")
  private final Map<String, Object> config;

  @JsonCreator
  public InitEvent(@JsonProperty("eventGroupId") UUID eventGroupId, @JsonProperty("node") DAGNode node,
      @JsonProperty("value") Map<String, Object> value, @JsonProperty("rootId") UUID rootId,
      @JsonProperty("config") Map<String, Object> config) {
    super();
    this.eventGroupId = eventGroupId;
    this.node = node;
    this.value = value;
    this.rootId = rootId;
    this.config = config;
  }

  public DAGNode getNode() {
    return node;
  }

  public Map<String, Object> getValue() {
    return value;
  }

  @Override
  public UUID getRootId() {
    return rootId;
  }
  
  @Override
  public UUID getEventGroupId() {
    return eventGroupId;
  }
  
  public Map<String, Object> getConfig() {
    return config;
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
    result = prime * result + ((node == null) ? 0 : node.hashCode());
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
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (!node.equals(other.node))
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
    return "InitEvent [node=" + node + ", value=" + value + ", rootId=" + rootId + ", config=" + config + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return PersistentEventType.INIT;
  }

}
