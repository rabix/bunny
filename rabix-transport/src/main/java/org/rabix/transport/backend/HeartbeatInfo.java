package org.rabix.transport.backend;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HeartbeatInfo {
  @JsonProperty("id")
  private UUID id;
  @JsonProperty("timestamp")
  private Long timestamp;

  @JsonCreator
  public HeartbeatInfo() {
  }

  public HeartbeatInfo(@JsonProperty("id") UUID id, @JsonProperty("timestamp") Long timestamp) {
    this.id = id;
    this.timestamp = timestamp;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
