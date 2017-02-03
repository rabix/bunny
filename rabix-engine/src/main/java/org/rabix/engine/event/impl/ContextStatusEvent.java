package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;
import org.rabix.engine.model.ContextRecord.ContextStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ContextStatusEvent implements Event {

  @JsonProperty("contextId")
  private final String contextId;
  @JsonProperty("status")
  private final ContextStatus status;
  
  @JsonCreator
  public ContextStatusEvent(@JsonProperty("contextId") String contextId, @JsonProperty("status") ContextStatus status) {
    this.status = status;
    this.contextId = contextId;
  }
  
  @Override
  public EventType getType() {
    return EventType.CONTEXT_STATUS_UPDATE;
  }

  public ContextStatus getStatus() {
    return status;
  }
  
  @Override
  public String getContextId() {
    return contextId;
  }
  
  @Override
  public String getEventGroupId() {
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
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
    ContextStatusEvent other = (ContextStatusEvent) obj;
    if (contextId == null) {
      if (other.contextId != null)
        return false;
    } else if (!contextId.equals(other.contextId))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ContextStatusEvent [contextId=" + contextId + ", status=" + status + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return null;
  }

}
