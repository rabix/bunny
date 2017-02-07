package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is used to update one output (per port) for the specific Job. Potentially, it can produce one ore more output and inputs events. 
 */
public class OutputUpdateEvent implements Event {

  @JsonProperty("jobName")
  private final String jobName;

  @JsonProperty("rootId")
  private final UUID rootId;

  @JsonProperty("value")
  private final Object value;
  @JsonProperty("portId")
  private final String portId;
  @JsonProperty("position")
  private final Integer position;
  @JsonProperty("fromScatter")
  private final boolean fromScatter;            // it's a scatter event
  @JsonProperty("numberOfScattered")
  private final Integer numberOfScattered;      // number of scattered nodes

  @JsonProperty("eventGroupId")
  private final UUID eventGroupId;

  public OutputUpdateEvent(UUID rootId, String jobName, String portId, Object value, Integer position, UUID eventGroupId) {
    this(jobName, rootId, value, portId, null, false, position, eventGroupId);
  }

  
  @JsonCreator
  public OutputUpdateEvent(@JsonProperty("jobName") String jobName, @JsonProperty("rootId") UUID rootId,
      @JsonProperty("value") Object value, @JsonProperty("portId") String portId,
      @JsonProperty("position") Integer position, @JsonProperty("fromScatter") boolean fromScatter,
      @JsonProperty("numberOfScattered") Integer numberOfScattered, @JsonProperty("eventGroupId") UUID eventGroupId) {
    this.jobName = jobName;
    this.rootId = rootId;
    this.value = value;
    this.portId = portId;
    this.position = position;
    this.fromScatter = fromScatter;
    this.numberOfScattered = numberOfScattered;
    this.eventGroupId = eventGroupId;
  }

  public String getJobName() {
    return jobName;
  }
  
  public Object getValue() {
    return value;
  }
  
  public String getPortId() {
    return portId;
  }
  
  public Integer getPosition() {
    return position;
  }

  public boolean isFromScatter() {
    return fromScatter;
  }
  
  public Integer getNumberOfScattered() {
    return numberOfScattered;
  }
  
  @Override
  public UUID getEventGroupId() {
    return eventGroupId;
  }
  
  @Override
  public UUID getRootId() {
    return rootId;
  }
  
  @Override
  public EventType getType() {
    return EventType.OUTPUT_UPDATE;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + (fromScatter ? 1231 : 1237);
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
    OutputUpdateEvent other = (OutputUpdateEvent) obj;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (fromScatter != other.fromScatter)
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
    return "OutputUpdateEvent [jobName=" + jobName + ", contextId=" + rootId + ", portId=" + portId + ", value=" + value + ", fromScatter=" + fromScatter + ", numberOfScattered=" + numberOfScattered + "]";
  }

  @Override
  public PersistentEventType getPersistentType() {
    return null;
  }
  
}
