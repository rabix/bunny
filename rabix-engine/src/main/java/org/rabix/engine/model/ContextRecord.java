package org.rabix.engine.model;

import java.util.Map;
import java.util.UUID;

public class ContextRecord {

  public static enum ContextStatus {
    RUNNING,
    COMPLETED,
    FAILED
  }
  
  private UUID id;
  private String externalId;
  private Map<String, Object> config;
  private ContextStatus status;
  
  public ContextRecord(UUID id, final String externalId, Map<String, Object> config, ContextStatus status) {
    this.id = id;
    this.externalId = externalId;
    this.config = config;
    this.status = status;
  }
  
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
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
