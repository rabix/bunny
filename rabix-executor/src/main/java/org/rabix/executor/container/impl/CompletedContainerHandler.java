package org.rabix.executor.container.impl;

import java.io.File;

import org.rabix.bindings.model.Job;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;

public class CompletedContainerHandler implements ContainerHandler {

  private Job job;
  
  public CompletedContainerHandler(Job job) {
    this.job = job;
  }

  @Override
  public void start() throws ContainerException {
    VerboseLogger.log(String.format("Job %s will be executed without a container. It is an expression tool.", job.getName()));
  }

  @Override
  public void stop() throws ContainerException {
  }

  @Override
  public boolean isStarted() throws ContainerException {
    return true;
  }

  @Override
  public boolean isRunning() throws ContainerException {
    return false;
  }

  @Override
  public int getProcessExitStatus() throws ContainerException {
    return 0;
  }

  @Override
  public void dumpContainerLogs(File errorFile) throws ContainerException {
  }

  @Override
  public void dumpCommandLine() throws ContainerException {
  }

}
