package org.rabix.engine.status.impl;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpEngineStatusCallback implements EngineStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(NoOpEngineStatusCallback.class);
  
  @Override
  public void onJobReady(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobReady(jobId={})", job.getId());
  }
  
  @Override
  public void onJobsReady(Set<Job> jobs) throws EngineStatusCallbackException {
    logger.debug("onJobsReady()");
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
  public void onJobRootPartiallyCompleted(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobRootPartiallyCompleted(jobId={})", rootJob.getId());
  }

  @Override
  public void onJobRootFailed(Job rootJob) throws EngineStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", rootJob.getId());
  }

}
