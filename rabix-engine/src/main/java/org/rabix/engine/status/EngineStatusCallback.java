package org.rabix.engine.status;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface EngineStatusCallback {

  void onJobReady(Job job) throws EngineStatusCallbackException;

  void onJobsReady(Set<Job> jobs, UUID rootId, String producedByNode) throws EngineStatusCallbackException;
  
  void onJobCompleted(Job job) throws EngineStatusCallbackException;
  
  void onJobFailed(Job job) throws EngineStatusCallbackException;

  void onJobContainerReady(Job rootJob) throws EngineStatusCallbackException;
  
  void onJobRootCompleted(Job rootJob) throws EngineStatusCallbackException;
  
  void onJobRootPartiallyCompleted(Job rootJob, String producedBy) throws EngineStatusCallbackException;
  
  void onJobRootFailed(Job rootJob) throws EngineStatusCallbackException;
  
}
