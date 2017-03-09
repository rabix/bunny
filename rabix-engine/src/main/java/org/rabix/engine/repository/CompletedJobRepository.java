package org.rabix.engine.repository;

import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface CompletedJobRepository {

  void insert(Job job);
  
  void insert(Job job, UUID backendId);
  
  public class CompletedJobEntity {
    
    Job job;
    UUID backendId;
    
    public CompletedJobEntity(Job job, UUID backendId) {
      super();
      this.job = job;
      this.backendId = backendId;
    }

    public CompletedJobEntity(Job job) {
      super();
      this.job = job;
    }

    public Job getJob() {
      return job;
    }

    public void setJob(Job job) {
      this.job = job;
    }
    public UUID getBackendId() {
      return backendId;
    }

    public void setBackendId(UUID backendId) {
      this.backendId = backendId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((job == null) ? 0 : job.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      CompletedJobEntity other = (CompletedJobEntity) obj;
      if (job == null) {
        if (other.job != null)
          return false;
      } else if (!job.equals(other.job))
        return false;
      return true;
    }

  }

}