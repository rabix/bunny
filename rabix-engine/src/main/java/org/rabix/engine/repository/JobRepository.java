package org.rabix.engine.repository;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.jdbi.impl.JDBIJobRepository.JobBackendPair;

public interface JobRepository {

  void insert(Job job, UUID groupId);
  
  void update(Job job);
  
  void updateBackendId(UUID jobId, UUID backendId);
  
  void updateBackendIds(Iterator<JobBackendPair> jobBackendPair);
  
  void dealocateJobs(UUID backendId);
  
  Job get(UUID id);
  
  Set<Job> get();
  
  Set<Job> getByRootId(UUID rootId);
  
  Set<UUID> getBackendsByRootId(UUID rootId);
  
  UUID getBackendId(UUID jobId);
  
  Set<Job> getReadyJobsByGroupId(UUID groupId);

  Set<Job> getReadyFree();
  
  JobStatus getStatus(UUID id);
  
  public class JobEntity {
    
    Job job;
    UUID groupId;
    UUID backendId;
    
    public JobEntity(Job job, UUID groupId, UUID backendId) {
      super();
      this.job = job;
      this.groupId = groupId;
      this.backendId = backendId;
    }

    public JobEntity(Job job, UUID groupId) {
      super();
      this.job = job;
      this.groupId = groupId;
      this.backendId = null;
    }

    public Job getJob() {
      return job;
    }

    public void setJob(Job job) {
      this.job = job;
    }

    public UUID getGroupId() {
      return groupId;
    }

    public void setGroupId(UUID groupId) {
      this.groupId = groupId;
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
      result = prime * result + ((backendId == null) ? 0 : backendId.hashCode());
      result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
      JobEntity other = (JobEntity) obj;
      if (backendId == null) {
        if (other.backendId != null)
          return false;
      } else if (!backendId.equals(other.backendId))
        return false;
      if (groupId == null) {
        if (other.groupId != null)
          return false;
      } else if (!groupId.equals(other.groupId))
        return false;
      if (job == null) {
        if (other.job != null)
          return false;
      } else if (!job.equals(other.job))
        return false;
      return true;
    }
    @Override
    public String toString() {
      return "JobEntity [job=" + job + ", groupId=" + groupId + ", backendId=" + backendId + "]";
    }
  }
  
}