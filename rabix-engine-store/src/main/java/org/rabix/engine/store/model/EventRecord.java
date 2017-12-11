package org.rabix.engine.store.model;

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

  private UUID rootId;
  private UUID groupId;
  private Status status;
  private Map<String, ?> event;

  public EventRecord(UUID rootId, UUID groupId, Status status, Map<String, ?> event) {
    this.rootId = rootId;
    this.groupId = groupId;
    this.status = status;
    this.event = event;
  }

  public UUID getRootId() {
    return rootId;
  }

  public void setRootId(UUID rootId) {
    this.rootId = rootId;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
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
