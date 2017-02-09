package org.rabix.engine.model;

import java.util.Map;

public class ContextRecord {

  public static enum ContextStatus {
    RUNNING,
    COMPLETED,
    FAILED
  }
  
  private String id;
  private Map<String, Object> config;
  private ContextStatus status;
  
  public ContextRecord(final String id, Map<String, Object> config, ContextStatus status) {
    this.id = id;
    this.config = config;
    this.status = status;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public ContextStatus getStatus() {
    return status;
  }

  public void setStatus(ContextStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "ContextRecord [id=" + id + ", config=" + config + ", status=" + status + "]";
  }

}
