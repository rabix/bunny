package org.rabix.engine.event.impl;

import org.rabix.engine.event.Event;

/**
 * This event is used to update one output (per port) for the specific Job. Potentially, it can produce one ore more output and inputs events. 
 */
public class OutputUpdateEvent implements Event {

  private final String jobId;
  private final String contextId;
  
  private final Object value;
  private final String portId;
  
  private final Integer position;
  private final boolean fromScatter;            // it's a scatter event
  private final Integer numberOfScattered;      // number of scattered nodes
  
  private final String eventGroupId;

  public OutputUpdateEvent(String contextId, String jobId, String portId, Object value, Integer position, String eventGroupId) {
    this(contextId, jobId, portId, value, false, null, position, eventGroupId);
  }
  
  public OutputUpdateEvent(String contextId, String jobId, String portId, Object outputValue, boolean fromScatter, Integer numberOfScattered, Integer position, String eventGroupId) {
    this.jobId = jobId;
    this.contextId = contextId;
    this.portId = portId;
    this.value = outputValue;
    this.position = position;
    this.fromScatter = fromScatter;
    this.eventGroupId = eventGroupId;
    this.numberOfScattered = numberOfScattered;
  }
  
  public String getJobId() {
    return jobId;
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
  public String getEventGroupId() {
    return eventGroupId;
  }
  
  @Override
  public String getContextId() {
    return contextId;
  }
  
  @Override
  public EventType getType() {
    return EventType.OUTPUT_UPDATE;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = prime * result + (fromScatter ? 1231 : 1237);
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
    OutputUpdateEvent other = (OutputUpdateEvent) obj;
    if (contextId == null) {
      if (other.contextId != null)
        return false;
    } else if (!contextId.equals(other.contextId))
      return false;
    if (fromScatter != other.fromScatter)
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
    return "OutputUpdateEvent [jobId=" + jobId + ", contextId=" + contextId + ", portId=" + portId + ", value=" + value + ", fromScatter=" + fromScatter + ", numberOfScattered=" + numberOfScattered + "]";
  }
  
}
