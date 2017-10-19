package org.rabix.engine.status;

import java.util.Map;
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
  
  void onJobRootPartiallyCompleted(UUID rootId, Map<String, Object> outputs, String producedBy) throws EngineStatusCallbackException;

  void onJobRootFailed(Job rootJob) throws EngineStatusCallbackException;
  
  void onJobRootAborted(Job rootJob) throws EngineStatusCallbackException;
  
}
