package org.rabix.engine.repository;

import java.util.Set;

import org.rabix.engine.db.JobBackendService.BackendJob;

public interface JobBackendRepository {

  int insert(String jobId, String rootId, String backendId);
  
  int update(String jobId, String backendId);
  
  int delete(String jobId);
  
  BackendJob getByJobId(String jobId);
  
  Set<BackendJob> getByRootId(String rootId);
  
  Set<BackendJob> getByBackendId(String backendId);
  
  Set<BackendJob> getFreeJobs();
  
  Set<BackendJob> getFreeJobs(String rootId);

}
