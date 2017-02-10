package org.rabix.engine.db;

import java.util.Set;
import java.util.UUID;

import org.rabix.engine.repository.JobBackendRepository;

import com.google.inject.Inject;

public class JobBackendService {

  private JobBackendRepository jobBackendRepository;
  
  @Inject
  public JobBackendService(JobBackendRepository jobBackendRepository) {
    this.jobBackendRepository = jobBackendRepository;
  }
  
  public void insert(UUID jobId, UUID rootId, UUID backendId) {
    this.jobBackendRepository.insert(jobId, rootId, backendId);
  }
  
  public void delete(UUID jobId) {
    this.jobBackendRepository.delete(jobId);
  }
  
  public void update(UUID jobId, UUID backendId) {
    this.jobBackendRepository.update(jobId, backendId);
  }
  
  public BackendJob getByJobId(UUID jobId) {
    return jobBackendRepository.getByJobId(jobId);
  }
  
  public Set<BackendJob> getByRootId(UUID rootId) {
    return jobBackendRepository.getByRootId(rootId);
  }
  
  public Set<BackendJob> getByBackendId(UUID backendId) {
    return jobBackendRepository.getByBackendId(backendId);
  }
  
  public Set<BackendJob> getFree() {
    return jobBackendRepository.getFreeJobs();
  }
  
  public Set<BackendJob> getFree(UUID rootId) {
    return jobBackendRepository.getFreeJobs(rootId);
  }
  
  public static class BackendJob {
    private UUID jobId;
    private UUID rootId;
    private UUID backendId;
    
    public BackendJob(UUID jobId, UUID rootId, UUID backendId) {
      super();
      this.jobId = jobId;
      this.rootId = rootId;
      this.backendId = backendId;
    }

    public UUID getJobId() {
      return jobId;
    }

    public void setJobId(UUID jobId) {
      this.jobId = jobId;
    }

    public UUID getRootId() {
      return rootId;
    }

    public void setRootId(UUID rootId) {
      this.rootId = rootId;
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
      result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
      result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
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
      BackendJob other = (BackendJob) obj;
      if (backendId == null) {
        if (other.backendId != null)
          return false;
      } else if (!backendId.equals(other.backendId))
        return false;
      if (jobId == null) {
        if (other.jobId != null)
          return false;
      } else if (!jobId.equals(other.jobId))
        return false;
      if (rootId == null) {
        if (other.rootId != null)
          return false;
      } else if (!rootId.equals(other.rootId))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "BackendJob [jobId=" + jobId + ", rootId=" + rootId + ", backendId=" + backendId + "]";
    }
    
  }
  
}
