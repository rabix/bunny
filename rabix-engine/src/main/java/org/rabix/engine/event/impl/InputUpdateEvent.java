package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is used to update one input (per port) for the specific Job. It triggers the algorithm cycle.
 */
public class InputUpdateEvent implements Event {

  @JsonProperty("jobName")
  private final String jobName;

  @JsonProperty("rootId")
  private final UUID rootId;
  
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

  public InputUpdateEvent(UUID rootId, String jobName, String portId, Object value, Integer position, UUID eventGroupId) {
    this(jobName, rootId, portId, value, position, null, false, eventGroupId);
  }

  @JsonCreator
  public InputUpdateEvent(@JsonProperty("jobName") String jobName, @JsonProperty("rootId") UUID rootId,
      @JsonProperty("portId") String portId, @JsonProperty("value") Object value,
      @JsonProperty("position") Integer position, @JsonProperty("numberOfScattered") Integer numberOfScattered,
      @JsonProperty("isLookAhead") boolean isLookAhead, @JsonProperty("eventGroupId") UUID eventGroupId) {
    this.jobName = jobName;
    this.rootId = rootId;
    this.portId = portId;
    this.value = value;
    this.position = position;
    this.numberOfScattered = numberOfScattered;
    this.isLookAhead = isLookAhead;
    this.eventGroupId = eventGroupId;
  }

  public String getJobId() {
    return jobName;
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
  public UUID getRootId() {
    return rootId;
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
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + (isLookAhead ? 1231 : 1237);
    result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
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
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (isLookAhead != other.isLookAhead)
      return false;
    if (jobName == null) {
      if (other.jobName != null)
        return false;
    } else if (!jobName.equals(other.jobName))
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
    return "InputUpdateEvent [jobName=" + jobName + ", contextId=" + rootId + ", portId=" + portId + ", value=" + value + ", numberOfScattered=" + numberOfScattered + ", isLookAhead=" + isLookAhead + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return null;
  }

}
