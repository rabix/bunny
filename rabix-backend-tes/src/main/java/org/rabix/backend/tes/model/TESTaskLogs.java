package org.rabix.backend.tes.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTaskLogs {
  @JsonProperty("logs")
  private List<TESJobLog> logs;
  @JsonProperty("start_time")
  private String startTime;
  @JsonProperty("end_time")
  private String endTime;
  @JsonProperty("outputs")
  private List<Map<String, String>> outputs;

  public TESTaskLogs(@JsonProperty("logs") List<TESJobLog> logs, @JsonProperty("start_time") String startTime, @JsonProperty("end_time") String endTime,
      @JsonProperty("outputs") List<Map<String, String>> outputs) {
    super();
    this.logs = logs;
    this.startTime = startTime;
    this.endTime = endTime;
    this.outputs = outputs;
  }

  public List<TESJobLog> getLogs() {
    return logs;
  }

  public void setLogs(List<TESJobLog> logs) {
    this.logs = logs;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public List<Map<String, String>> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<Map<String, String>> outputs) {
    this.outputs = outputs;
  }

}
