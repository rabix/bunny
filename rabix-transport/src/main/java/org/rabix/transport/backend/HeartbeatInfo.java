package org.rabix.transport.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HeartbeatInfo {
  @JsonProperty("id")
  private String id;
  @JsonProperty("timestamp")
  private Long timestamp;
  
  @JsonCreator
  public HeartbeatInfo(@JsonProperty("id") String id, @JsonProperty("timestamp") Long timestamp) {
    this.id = id;
    this.timestamp = timestamp;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}