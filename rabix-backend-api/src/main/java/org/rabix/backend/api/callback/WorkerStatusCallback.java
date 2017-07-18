package org.rabix.backend.api.callback;

import org.rabix.bindings.model.Job;

public interface WorkerStatusCallback {

  Job onJobReady(Job job) throws WorkerStatusCallbackException;
  
  Job onJobFailed(Job job) throws WorkerStatusCallbackException;
  
  Job onJobStarted(Job job) throws WorkerStatusCallbackException;
  
  Job onJobStopped(Job job) throws WorkerStatusCallbackException;
  
  Job onJobCompleted(Job job) throws WorkerStatusCallbackException;
  
  void onContainerImagePullStarted(Job job, String image) throws WorkerStatusCallbackException;
  
  void onContainerImagePullFailed(Job job, String image) throws WorkerStatusCallbackException;
  
  void onContainerImagePullCompleted(Job job, String image) throws WorkerStatusCallbackException;
  
  void onInputFilesDownloadStarted(Job job) throws WorkerStatusCallbackException;
  
  void onInputFilesDownloadFailed(Job job) throws WorkerStatusCallbackException;
  
  void onInputFilesDownloadCompleted(Job job) throws WorkerStatusCallbackException;
  
  void onOutputFilesUploadStarted(Job job) throws WorkerStatusCallbackException;
  
  void onOutputFilesUploadFailed(Job job) throws WorkerStatusCallbackException;
  
  void onOutputFilesUploadCompleted(Job job) throws WorkerStatusCallbackException;
  
}
