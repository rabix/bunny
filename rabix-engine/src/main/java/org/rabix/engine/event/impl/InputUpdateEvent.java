package org.rabix.engine.event.impl;

import java.util.UUID;

import org.rabix.engine.event.Event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is used to update one input (per port) for the specific Job. It triggers the algorithm cycle.
 */
public class InputUpdateEvent implements Event {

  @JsonProperty("jobId")
  private final String jobId;
  @JsonProperty("contextId")
  private final UUID contextId;
  @JsonProperty("portId")
  private final String portId;
  @JsonProperty("value")
  private final Object value;
  @JsonProperty("position")
  private final Integer position;
  @JsonProperty("numberOfScattered")
  private final Integer numberOfScattered;      // number of scattered nodes
  @JsonProperty("isLookAhead")
  private final boolean isLookAhead;            // it's a look ahead event
  @JsonProperty("eventGroupId")
  private final UUID eventGroupId;
  @JsonProperty("producedByNode")
  private final String producedByNode;
  
  public InputUpdateEvent(UUID contextId, String jobId, String portId, Object value, Integer position, UUID eventGroupId, String producedByNode) {
    this(contextId, jobId, portId, value, false, null, position, eventGroupId, producedByNode);
  }

  public InputUpdateEvent(UUID contextId, String jobId, String portId, Object value, boolean isLookAhead, Integer scatteredNodes, Integer position, UUID eventGroupId, String producedByNode) {
    this.jobId = jobId;
    this.portId = portId;
    this.value = value;
    this.contextId = contextId;
    this.isLookAhead = isLookAhead;
    this.numberOfScattered = scatteredNodes;
    this.position = position;
    this.eventGroupId = eventGroupId;
    this.producedByNode = producedByNode;
  }

  @JsonCreator
  public InputUpdateEvent(@JsonProperty("jobId") String jobId, @JsonProperty("contextId") UUID contextId,
      @JsonProperty("portId") String portId, @JsonProperty("value") Object value,
      @JsonProperty("position") Integer position, @JsonProperty("numberOfScattered") Integer numberOfScattered,
      @JsonProperty("isLookAhead") boolean isLookAhead, @JsonProperty("eventGroupId") UUID eventGroupId, @JsonProperty("producedByNode") String producedByNode) {
    this.jobId = jobId;
    this.contextId = contextId;
    this.portId = portId;
    this.value = value;
    this.position = position;
    this.numberOfScattered = numberOfScattered;
    this.isLookAhead = isLookAhead;
    this.eventGroupId = eventGroupId;
    this.producedByNode = producedByNode;
  }

  public String getJobId() {
    return jobId;
  }

  public String getPortId() {
    return portId;
  }

  public Object getValue() {
    return value;
  }
  
  public Integer getNumberOfScattered() {
    return numberOfScattered;
  }

  public boolean isLookAhead() {
    return isLookAhead;
  }

  @Override
  public UUID getEventGroupId() {
    return eventGroupId;
  }
  
  @Override
  public String getProducedByNode() {
    return producedByNode;
  }
  
  @Override
  public UUID getContextId() {
    return contextId;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  @Override
  public EventType getType() {
    return EventType.INPUT_UPDATE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = prime * result + (isLookAhead ? 1231 : 1237);
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
    result = prime * result + ((portId == null) ? 0 : portId.hashCode());
    result = prime * result + ((numberOfScattered == null) ? 0 : numberOfScattered.hashCode());
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
    InputUpdateEvent other = (InputUpdateEvent) obj;
    if (contextId == null) {
      if (other.contextId != null)
        return false;
    } else if (!contextId.equals(other.contextId))
      return false;
    if (isLookAhead != other.isLookAhead)
      return false;
    if (jobId == null) {
      if (other.jobId != null)
        return false;
    } else if (!jobId.equals(other.jobId))
      return false;
    if (portId == null) {
      if (other.portId != null)
        return false;
    } else if (!portId.equals(other.portId))
      return false;
    if (numberOfScattered == null) {
      if (other.numberOfScattered != null)
        return false;
    } else if (!numberOfScattered.equals(other.numberOfScattered))
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
    return "InputUpdateEvent [jobId=" + jobId + ", contextId=" + contextId + ", portId=" + portId + ", value=" + value + ", numberOfScattered=" + numberOfScattered + ", isLookAhead=" + isLookAhead + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return null;
  }

}
