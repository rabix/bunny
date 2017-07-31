package org.rabix.engine.status.impl;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEngineStatusCallback implements EngineStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(DefaultEngineStatusCallback.class);
  
  @Override
  public void onJobReady(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobReady(jobId={})", job.getId());
  }
  
  @Override
  public void onJobsReady(Set<Job> jobs, UUID rootId, String producedByNode) throws EngineStatusCallbackException {
    for (Job job : jobs) {
      onJobReady(job);
    }
  }

  @Override
  public void onJobFailed(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", job.getId());
  }

  @Override
  public void onJobCompleted(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobCompleted(jobId={})", job.getId());
  }
  
  @Override
  public void onJobRootCompleted(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobRootCompleted(jobId={})", rootJob.getId());
  }
  
  @Override
  public void onJobRootPartiallyCompleted(UUID rootId, Map<String,Object> outputs, String producedBy) throws EngineStatusCallbackException {
    logger.debug("onJobRootPartiallyCompleted(jobId={})", rootId);
  }

  @Override
  public void onJobRootFailed(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", rootJob.getId());
  }
  
  @Override
  public void onJobRootAborted(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobAborted(jobId={})", rootJob.getId());
  }

  @Override
  public void onJobContainerReady(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobRootReady(jobId={})", rootJob.getId());
  }

}
