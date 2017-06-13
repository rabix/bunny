package org.rabix.backend.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TESJobLog {
  
  @JsonProperty("cmd")
  private List<String> commandLineParts;
  @JsonProperty("start_time")
  private String startTime;
  @JsonProperty("end_time")
  private String endTime;
  @JsonProperty("stdout")
  private String stdout;
  @JsonProperty("stderr")
  private String stderr;
  @JsonProperty("exit_code")
  private Integer exitCode;
  
  public TESJobLog(@JsonProperty("cmd") List<String> commandLineParts, @JsonProperty("start_time") String startTime, @JsonProperty("end_time") String endTime, @JsonProperty("stdout") String stdout, @JsonProperty("stderr") String stderr,
      @JsonProperty("exit_code") Integer exitCode) {
    super();
    this.commandLineParts = commandLineParts;
    this.startTime = startTime;
    this.endTime = endTime;
    this.stdout = stdout;
    this.stderr = stderr;
    this.exitCode = exitCode;
  }

  public List<String> getCommandLineParts() {
    return commandLineParts;
  }

  public void setCommandLineParts(List<String> commandLineParts) {
    this.commandLineParts = commandLineParts;
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

  public String getStdout() {
    return stdout;
  }

  public void setStdout(String stdout) {
    this.stdout = stdout;
  }

  public String getStderr() {
    return stderr;
  }

  public void setStderr(String stderr) {
    this.stderr = stderr;
  }

  public Integer getExitCode() {
    return exitCode;
  }

  public void setExitCode(Integer exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public String toString() {
    return "TESJobLog [commandLineParts=" + commandLineParts + ", startTime=" + startTime
        + ", endTime=" + endTime + ", stdout=" + stdout + ", stderr=" + stderr + ", exitCode=" + exitCode + "]";
  }
  
}
