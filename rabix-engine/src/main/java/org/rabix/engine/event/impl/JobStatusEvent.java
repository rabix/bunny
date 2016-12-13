package org.rabix.engine.event.impl;

import java.util.Map;

import org.rabix.engine.event.Event;
import org.rabix.engine.service.JobRecordService.JobState;

public class JobStatusEvent implements Event {

  private final String jobId;
  private final JobState state;
  private final String contextId;
  
  private final Map<String, Object> result;
  
  private final String eventGroupId;
  
  public JobStatusEvent(String jobId, String contextId, JobState state, Map<String, Object> result, String eventGroupId) {
    this.jobId = jobId;
    this.contextId = contextId;
    this.state = state;
    this.result = result;
    this.eventGroupId = eventGroupId;
  }
  
  public String getJobId() {
    return jobId;
  }
  
  public JobState getState() {
    return state;
  }

  @Override
  public String getContextId() {
    return contextId;
  }
  
  public Map<String, Object> getResult() {
    return result;
  }
  
  @Override
  public String getEventGroupId() {
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
    result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
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
    if (contextId == null) {
      if (other.contextId != null)
        return false;
    } else if (!contextId.equals(other.contextId))
      return false;
    if (jobId == null) {
      if (other.jobId != null)
        return false;
    } else if (!jobId.equals(other.jobId))
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
    return "JobStatusEvent [jobId=" + jobId + ", state=" + state + ", contextId=" + contextId + ", result=" + result + "]";
  }

}
