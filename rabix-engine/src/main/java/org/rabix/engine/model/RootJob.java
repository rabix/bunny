package org.rabix.engine.model;

import java.util.Map;
import java.util.UUID;

public class RootJob {

  public static enum RootJobStatus {
    RUNNING,
    COMPLETED,
    FAILED
  }
  
  private UUID id;
  private Map<String, Object> config;
  private RootJobStatus status;
  
  public RootJob(UUID id, Map<String, Object> config, RootJobStatus status) {
    this.id = id;
    this.config = config;
    this.status = status;
  }
  
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public RootJobStatus getStatus() {
    return status;
  }

  public void setStatus(RootJobStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "RootJob [id=" + id + ", config=" + config + ", status=" + status + "]";
  }

}
