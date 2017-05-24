package org.rabix.backend.api.callback;

import org.rabix.bindings.model.Job;

public interface ExecutorStatusCallback {

  Job onJobReady(Job job) throws ExecutorStatusCallbackException;
  
  Job onJobFailed(Job job) throws ExecutorStatusCallbackException;
  
  Job onJobStarted(Job job) throws ExecutorStatusCallbackException;
  
  Job onJobStopped(Job job) throws ExecutorStatusCallbackException;
  
  Job onJobCompleted(Job job) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullStarted(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullFailed(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullCompleted(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadCompleted(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadCompleted(Job job) throws ExecutorStatusCallbackException;
  
}
