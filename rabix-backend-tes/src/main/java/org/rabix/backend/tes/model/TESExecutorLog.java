package org.rabix.backend.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TESExecutorLog {

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
  
  public TESExecutorLog(@JsonProperty("start_time") String startTime, @JsonProperty("end_time") String endTime, @JsonProperty("stdout") String stdout, @JsonProperty("stderr") String stderr,
                        @JsonProperty("exit_code") Integer exitCode) {
    super();
    this.startTime = startTime;
    this.endTime = endTime;
    this.stdout = stdout;
    this.stderr = stderr;
    this.exitCode = exitCode;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getStdout() {
    return stdout;
  }

  public String getStderr() {
    return stderr;
  }

  public Integer getExitCode() {
    return exitCode;
  }

  @Override
  public String toString() {
    return "TESExecutorLog [startTime=" + startTime + ", endTime=" + endTime + ", stdout=" + stdout + ", stderr=" + stderr + ", exitCode=" + exitCode + "]";
  }
  
}
