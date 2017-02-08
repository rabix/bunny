package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;
import org.rabix.engine.model.RootJob.RootJobStatus;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class RootJobStatusEvent implements Event {

  @JsonProperty("rootId")
  private final UUID rootId;

  @JsonProperty("status")
  private final RootJobStatus status;
  
  @JsonCreator
  public RootJobStatusEvent(@JsonProperty("rootId") UUID rootId, @JsonProperty("status") RootJobStatus status) {
    this.status = status;
    this.rootId = rootId;
  }
  
  @Override
  public EventType getType() {
    return EventType.CONTEXT_STATUS_UPDATE;
  }

  public RootJobStatus getStatus() {
    return status;
  }
  
  @Override
  public UUID getRootId() {
    return rootId;
  }
  
  @Override
  public UUID getEventGroupId() {
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
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
    RootJobStatusEvent other = (RootJobStatusEvent) obj;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RootJobStatusEvent [contextId=" + rootId + ", status=" + status + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return null;
  }

}
