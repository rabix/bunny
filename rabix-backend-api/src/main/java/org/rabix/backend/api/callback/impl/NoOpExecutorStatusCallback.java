package org.rabix.backend.api.callback.impl;

import org.rabix.backend.api.callback.ExecutorStatusCallback;
import org.rabix.backend.api.callback.ExecutorStatusCallbackException;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpExecutorStatusCallback implements ExecutorStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(NoOpExecutorStatusCallback.class);

  @Override
  public Job onJobReady(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobReady(jobId={})", job.getId());
    return job;
  }
  
  @Override
  public Job onJobFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", job.getId());
    return job;
  }

  @Override
  public Job onJobStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobStarted(jobId={})", job.getId());
    return job;
  }
  
  @Override
  public Job onJobStopped(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobStopped(jobId={})", job.getId());
    return job;
  }

  @Override
  public Job onJobCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobCompleted(jobId={})", job.getId());
    return job;
  }

  @Override
  public void onContainerImagePullStarted(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullStarted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onContainerImagePullCompleted(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullCompleted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onInputFilesDownloadStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadStarted(jobId={})", job.getId());
  }

  @Override
  public void onInputFilesDownloadCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadCompleted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadStarted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadComplted(jobId={})", job.getId());
  }

  @Override
  public void onContainerImagePullFailed(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullFailed(jobId={})", job.getId());
  }

  @Override
  public void onInputFilesDownloadFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadFailed(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadFailed(jobId={})", job.getId());
  }

}
