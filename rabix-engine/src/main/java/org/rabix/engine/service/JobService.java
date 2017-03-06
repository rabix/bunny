package org.rabix.engine.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.repository.JobRepository.JobEntity;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException;

  Job start(Job job, Map<String, Object> config) throws JobServiceException;
  
  void stop(UUID id) throws JobServiceException;
  
  Set<Job> get();
  
  void delete(UUID jobId);

  void updateBackend(UUID jobId, UUID backendId);

  void updateBackends(Set<JobEntity> entities);
  
  Set<UUID> getBackendsByRootId(UUID rootId);

  void dealocateJobs(UUID backendId);

  Set<JobEntity> getReadyFree();

  Job get(UUID id);

  void handleJobCompleted(Job job);

  void handleJobRootPartiallyCompleted(Job rootJob);

  void handleJobRootFailed(Job job);

  void handleJobRootCompleted(Job job);

  void handleJobFailed(Job failedJob);

  void handleJobsReady(Set<Job> jobs);

  void handleJobReady(Job job);

  void handleJobContainerReady(Job containerJob);

}
