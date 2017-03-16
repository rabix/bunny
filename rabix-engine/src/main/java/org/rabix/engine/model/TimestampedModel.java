package org.rabix.engine.model;

import java.time.LocalDateTime;

/**
 * Created by luka on 15.3.17..
 */
public class TimestampedModel {
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public TimestampedModel(LocalDateTime createdAt, LocalDateTime modifiedAt) {
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(LocalDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }
}
