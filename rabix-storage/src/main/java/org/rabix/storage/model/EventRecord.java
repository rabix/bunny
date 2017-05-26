package org.rabix.storage.model;

import java.util.Map;
import java.util.UUID;

/**
 * Created by luka on 22.5.17..
 */
public class EventRecord {

  public enum Status {
    PROCESSED,
    UNPROCESSED,
    FAILED
  }

  public enum PersistentType {
    INIT,
    JOB_STATUS_UPDATE_RUNNING,
    JOB_STATUS_UPDATE_COMPLETED
  }

  private UUID groupId;
  private PersistentType type;
  private Status status;
  private Map<String, ?> event;

  public EventRecord(UUID groupId, PersistentType type, Status status, Map<String, ?> event) {
    this.groupId = groupId;
    this.type = type;
    this.status = status;
    this.event = event;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
  }

  public PersistentType getType() {
    return type;
  }

  public void setType(PersistentType type) {
    this.type = type;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Map<String, ?> getEvent() {
    return event;
  }

  public void setEvent(Map<String, ?> event) {
    this.event = event;
  }

}
