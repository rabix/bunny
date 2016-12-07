package org.rabix.backend.local.tes.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESJob {

  @JsonProperty("jobID")
  private String tesJobId;
  @JsonProperty("metadata")
  private Map<String, String> metadata;
  @JsonProperty("task")
  private TESTask task;
  @JsonProperty("state")
  private TESState state;
  @JsonProperty("logs")
  private List<TESJobLog> logs;

  @JsonCreator
  public TESJob(@JsonProperty("jobID") String tesJobId, @JsonProperty("metadata") Map<String, String> metadata, @JsonProperty("task") TESTask task, @JsonProperty("state") TESState state, @JsonProperty("logs") List<TESJobLog> logs) {
    this.tesJobId = tesJobId;
    this.metadata = metadata;
    this.task = task;
    this.state = state;
    this.logs = logs;
  }

  public String getTesJobId() {
    return tesJobId;
  }

  public void setTesJobId(String tesJobId) {
    this.tesJobId = tesJobId;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public TESTask getTask() {
    return task;
  }

  public void setTask(TESTask task) {
    this.task = task;
  }

  public TESState getState() {
    return state;
  }

  public void setState(TESState state) {
    this.state = state;
  }

  public List<TESJobLog> getLogs() {
    return logs;
  }

  public void setLogs(List<TESJobLog> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "TESJob [tesJobId=" + tesJobId + ", metadata=" + metadata + ", task=" + task + ", state=" + state + ", logs="
        + logs + "]";
  }
  
}
