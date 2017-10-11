package org.rabix.engine.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.store.repository.JobRepository.JobEntity;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException;

  Job start(Job job, Map<String, Object> config) throws JobServiceException;
  
  void stop(UUID id) throws JobServiceException;
  
  void delete(UUID jobId);

  void updateBackend(UUID jobId, UUID backendId);

  void updateBackends(Set<JobEntity> entities);
  
  Set<UUID> getBackendsByRootId(UUID rootId);

  void dealocateJobs(UUID backendId);

  Set<JobEntity> getReadyFree();

  Job get(UUID id);

  void handleJobCompleted(Job job);

  void handleJobRootPartiallyCompleted(UUID uuid, Map<String, Object> outputs, String producedBy);

  void handleJobRootFailed(Job job);

  void handleJobRootCompleted(Job job);

  void handleJobFailed(Job failedJob);

  void handleJobsReady(Set<Job> jobs, UUID rootId, String producedByNode);

  void handleJobContainerReady(Job containerJob);

  void handleJobRootAborted(Job rootJob);

}
