package org.rabix.engine.event.impl;

import java.util.Map;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.event.Event;

/**
 * This event is a starter event. It triggers the algorithm start. 
 */
public class InitEvent implements Event {

  private final DAGNode node;
  private final Map<String, Object> value;
  private final String rootId;
  private final Map<String, Object> config;
  
  public InitEvent(Map<String, Object> config, String rootId, DAGNode node, Map<String, Object> value) {
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
  
  public String getRootId() {
    return rootId;
  }
  
  @Override
  public String getContextId() {
    return rootId;
  }
  
  @Override
  public String getEventGroupId() {
    return null;
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

}
