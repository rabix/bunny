package org.rabix.backend.tes.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TESTaskLogs {
  @JsonProperty("logs")
  private List<TESExecutorLog> logs;
  @JsonProperty("metadata")
  private Map<String, String> metadata;
  @JsonProperty("start_time")
  private String startTime;
  @JsonProperty("end_time")
  private String endTime;
  @JsonProperty("outputs")
  private List<Map<String, String>> outputs;

  public TESTaskLogs(@JsonProperty("logs") List<TESExecutorLog> logs, @JsonProperty("metadata") Map<String, String> metadata, @JsonProperty("start_time") String startTime, @JsonProperty("end_time") String endTime,
      @JsonProperty("outputs") List<Map<String, String>> outputs) {
    super();
    this.logs = logs;
    this.metadata = metadata;
    this.startTime = startTime;
    this.endTime = endTime;
    this.outputs = outputs;
  }

  public List<TESExecutorLog> getLogs() {
    return logs;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public List<Map<String, String>> getOutputs() {
    return outputs;
  }
}
