package org.rabix.engine.store.model;

import java.util.UUID;

public class JobStatsRecord {

  private UUID rootId;
  private int completed;
  private int running;
  private int total;

  public JobStatsRecord(final UUID rootId, int completed, int running, int total) {
    this.rootId = rootId;
    this.completed = completed;
    this.running = running;
    this.total = total;
  }
  
  public UUID getRootId() {
    return rootId;
  }

  public void setRootId(UUID rootId) {
    this.rootId = rootId;
  }

  public int getCompleted() {
    return completed;
  }

  public void setCompleted(int completed) {
    this.completed = completed;
  }

  public int getRunning() {
    return running;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public void setRunning(int running) {
    this.running = running;
  }
  public void increaseRunning() {
    running++;
  }
  public void increaseCompleted() {
    completed++;
  }

  @Override public String toString() {
    return "JobStatsRecord{" + "rootId=" + rootId + ", completed=" + completed + ", running=" + running + ", total="
        + total + '}';
  }
}
