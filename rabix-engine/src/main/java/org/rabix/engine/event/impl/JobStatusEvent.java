package org.rabix.engine.event.impl;

import java.util.Map;
import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.model.JobRecord.JobState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobStatusEvent implements Event {

  @JsonProperty("jobName")
  private final String jobName;

  @JsonProperty("state")
  private final JobState state;

  @JsonProperty("rootId")
  private final UUID rootId;

  @JsonProperty("result")
  private final Map<String, Object> result;

  @JsonProperty("eventGroupId")
  private final UUID eventGroupId;

  @JsonCreator
  public JobStatusEvent(@JsonProperty("jobName") String jobName, @JsonProperty("state") JobState state,
      @JsonProperty("rootId") UUID rootId, @JsonProperty("result") Map<String, Object> result,
      @JsonProperty("eventGroupId") UUID eventGroupId) {
    this.jobName = jobName;
    this.state = state;
    this.rootId = rootId;
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

  @Override
  public PersistentEventType getPersistentType() {
    switch (state) {
    case RUNNING:
      return PersistentEventType.JOB_STATUS_UPDATE_RUNNING;
    case COMPLETED:
      return PersistentEventType.JOB_STATUS_UPDATE_COMPLETED;
    default:
      break;
    }
    return null;
  }

}
