package org.rabix.backend.api.callback.impl;

import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpWorkerStatusCallback implements WorkerStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(NoOpWorkerStatusCallback.class);

  @Override
  public Job onJobReady(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onJobReady(jobId={})", job.getId());
    return job;
  }
  
  @Override
  public Job onJobFailed(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onJobFailed(jobId={})", job.getId());
    return job;
  }

  @Override
  public Job onJobStarted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onJobStarted(jobId={})", job.getId());
    return job;
  }
  
  @Override
  public Job onJobStopped(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onJobStopped(jobId={})", job.getId());
    return job;
  }

  @Override
  public Job onJobCompleted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onJobCompleted(jobId={})", job.getId());
    return job;
  }

  @Override
  public void onContainerImagePullStarted(Job job, String image) throws WorkerStatusCallbackException {
//    logger.debug("onContainerImagePullStarted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onContainerImagePullCompleted(Job job, String image) throws WorkerStatusCallbackException {
//    logger.debug("onContainerImagePullCompleted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onInputFilesDownloadStarted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onInputFilesDownloadStarted(jobId={})", job.getId());
  }

  @Override
  public void onInputFilesDownloadCompleted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onInputFilesDownloadCompleted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadStarted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onOutputFilesUploadStarted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadCompleted(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onOutputFilesUploadComplted(jobId={})", job.getId());
  }

  @Override
  public void onContainerImagePullFailed(Job job, String image) throws WorkerStatusCallbackException {
//    logger.debug("onContainerImagePullFailed(jobId={})", job.getId());
  }

  @Override
  public void onInputFilesDownloadFailed(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onInputFilesDownloadFailed(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadFailed(Job job) throws WorkerStatusCallbackException {
//    logger.debug("onOutputFilesUploadFailed(jobId={})", job.getId());
  }

}
