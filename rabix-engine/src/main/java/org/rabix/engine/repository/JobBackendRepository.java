package org.rabix.engine.repository;

import java.util.Set;
import java.util.UUID;

import org.rabix.engine.db.JobBackendService.BackendJob;

public interface JobBackendRepository {

  int insert(UUID jobId, UUID rootId, UUID backendId);
  
  int update(UUID jobId, UUID backendId);
  
  int delete(UUID jobId);
  
  BackendJob getByJobId(UUID jobId);
  
  Set<BackendJob> getByRootId(UUID rootId);
  
  Set<BackendJob> getByBackendId(UUID backendId);
  
  Set<BackendJob> getFreeJobs();
  
  Set<BackendJob> getFreeJobs(UUID rootId);

}
