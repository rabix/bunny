package org.rabix.engine.store.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Created by luka on 22.5.17..
 */
public class BackendRecord {

  public enum Status {
    ACTIVE,
    INACTIVE
  }

  public enum Type {
    LOCAL,
    ACTIVE_MQ,
    RABBIT_MQ
  }

  private UUID id;
  private String name;
  private Instant heartbeatInfo;
  private Status status;
  private Type type;
  private Map<String, ?> backendConfig;

  public BackendRecord(UUID id, String name, Instant heartbeatInfo, Map<String, ?> backendConfig, Status status, Type type) {
    this.id = id;
    this.name = name;
    this.heartbeatInfo = heartbeatInfo;
    this.backendConfig = backendConfig;
    this.status = status;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Instant getHeartbeatInfo() {
    return heartbeatInfo;
  }

  public void setHeartbeatInfo(Instant heartbeatInfo) {
    this.heartbeatInfo = heartbeatInfo;
  }

  public Map<String, ?> getBackendConfig() {
    return backendConfig;
  }

  public void setBackendConfig(Map<String, ?> backendConfig) {
    this.backendConfig = backendConfig;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
