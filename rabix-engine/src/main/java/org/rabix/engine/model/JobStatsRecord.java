package org.rabix.engine.model;

import java.util.UUID;

public class JobStatsRecord {

  private UUID jobId;
  private int completed;
  private int running;

  public JobStatsRecord(final UUID jobId, int completed, int running) {
    this.jobId = jobId;
    this.completed = completed;
    this.running = running;
  }
  
  public UUID getJobId() {
    return jobId;
  }

  public void setJobId(UUID jobId) {
    this.jobId = jobId;
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
    return "JobStatsRecord{" + "jobId=" + jobId + ", completed=" + completed + ", running=" + running + '}';
  }
}
