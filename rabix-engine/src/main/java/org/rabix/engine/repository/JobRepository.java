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

  
}