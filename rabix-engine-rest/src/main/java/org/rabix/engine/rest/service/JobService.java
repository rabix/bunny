package org.rabix.engine.rest.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.jdbi.impl.JDBIJobRepository.JobBackendPair;
import org.rabix.engine.processor.EventProcessor;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException;

  Job start(Job job, Map<String, Object> config) throws JobServiceException;
  
  void stop(UUID id) throws JobServiceException;
  
  Set<Job> get();
  
  void delete(UUID jobId);

  void updateBackend(UUID jobId, UUID backendId);

  void updateBackends(List<JobBackendPair> jobBackendPairs);
  
  Set<UUID> getBackendsByRootId(UUID rootId);

  void dealocateJobs(UUID backendId);

  Set<Job> getReadyFree();

  Job get(UUID id);

}
