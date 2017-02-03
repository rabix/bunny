package org.rabix.engine.event.impl;

import java.util.Map;
import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.model.JobRecord.JobState;

public class JobStatusEvent implements Event {

  private final String jobName;
  private final JobState state;
  private final UUID rootId;
  
  private final Map<String, Object> result;
  
  private final UUID eventGroupId;
  
  public JobStatusEvent(String jobName, UUID rootId, JobState state, Map<String, Object> result, UUID eventGroupId) {
    this.jobName = jobName;
    this.rootId = rootId;
    this.state = state;
    this.result = result;
    this.eventGroupId = eventGroupId;
  }
  
  public String getJobName() {
    return jobName;
  }
  
  public JobState getState() {
    return state;
  }

  @Override
  public UUID getRootId() {
    return rootId;
  }
  
  public Map<String, Object> getResult() {
    return result;
  }
  
  @Override
  public UUID getEventGroupId() {
    return eventGroupId;
  }
  
  @Override
  public EventType getType() {
    return EventType.JOB_STATUS_UPDATE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
    result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
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
    JobStatusEvent other = (JobStatusEvent) obj;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (jobName == null) {
      if (other.jobName != null)
        return false;
    } else if (!jobName.equals(other.jobName))
      return false;
    if (result == null) {
      if (other.result != null)
        return false;
    } else if (!result.equals(other.result))
      return false;
    if (state != other.state)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobStatusEvent [jobName=" + jobName + ", state=" + state + ", contextId=" + rootId + ", result=" + result + "]";
  }

}
