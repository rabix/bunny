package org.rabix.engine.db;

import java.util.Set;

import org.rabix.engine.dao.JobBackendRepository;

import com.google.inject.Inject;

public class JobBackendService {

  private JobBackendRepository jobBackendRepository;
  
  @Inject
  public JobBackendService(JobBackendRepository jobBackendRepository) {
    this.jobBackendRepository = jobBackendRepository;
  }
  
  public void insert(String jobId, String rootId, String backendId) {
    this.jobBackendRepository.insert(jobId, rootId, backendId);
  }
  
  public void delete(String jobId) {
    this.jobBackendRepository.delete(jobId);
  }
  
  public void update(String jobId, String backendId) {
    this.jobBackendRepository.update(jobId, backendId);
  }
  
  public BackendJob getByJobId(String jobId) {
    return jobBackendRepository.getByJobId(jobId);
  }
  
  public Set<BackendJob> getByRootId(String rootId) {
    return jobBackendRepository.getByRootId(rootId);
  }
  
  public Set<BackendJob> getByBackendId(String backendId) {
    return jobBackendRepository.getByBackendId(backendId);
  }
  
  public Set<BackendJob> getFree() {
    return jobBackendRepository.getFreeJobs();
  }
  
  public Set<BackendJob> getFree(String rootId) {
    return jobBackendRepository.getFreeJobs(rootId);
  }
  
  public static class BackendJob {
    private String jobId;
    private String rootId;
    private String backendId;
    
    public BackendJob(String jobId, String rootId, String backendId) {
      super();
      this.jobId = jobId;
      this.rootId = rootId;
      this.backendId = backendId;
    }

    public String getJobId() {
      return jobId;
    }

    public void setJobId(String jobId) {
      this.jobId = jobId;
    }

    public String getRootId() {
      return rootId;
    }

    public void setRootId(String rootId) {
      this.rootId = rootId;
    }

    public String getBackendId() {
      return backendId;
    }

    public void setBackendId(String backendId) {
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
