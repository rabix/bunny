package org.rabix.engine.status;

import org.rabix.bindings.model.Job;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface EngineStatusCallback {

  void onJobReady(Job job) throws EngineStatusCallbackException;

  void onJobsReady(Set<Job> jobs, UUID rootId, String producedByNode) throws EngineStatusCallbackException;

  void onJobCompleted(UUID jobId, UUID rootId) throws EngineStatusCallbackException;

  void onJobFailed(UUID jobId, UUID rootId) throws EngineStatusCallbackException;

  void onJobContainerReady(UUID id, UUID rootId) throws EngineStatusCallbackException;

  void onJobRootCompleted(UUID rootId) throws EngineStatusCallbackException;

  void onJobRootPartiallyCompleted(UUID rootId, Map<String, Object> outputs, String producedBy) throws EngineStatusCallbackException;

  void onJobRootFailed(UUID rootId, String message) throws EngineStatusCallbackException;

  void onJobRootAborted(UUID rootId) throws EngineStatusCallbackException;

}
