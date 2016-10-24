package org.rabix.executor.container;

import java.io.File;

/**
 * Encapsulates container handling functionalities 
 */
public interface ContainerHandler {

  /**
   * Start container 
   */
  public void start() throws ContainerException;

  /**
   * Stop container 
   */
  public void stop() throws ContainerException;

  /**
   * Is container stared? 
   */
  public boolean isStarted() throws ContainerException;

  /**
   * Is container running? 
   */
  public boolean isRunning() throws ContainerException;

  /**
   * Get container exit status 
   */
  public int getProcessExitStatus() throws ContainerException;

  /**
   * Dumps command line into a file 
   */
  public void dumpCommandLine() throws ContainerException;
  
  /**
   * Do post-processing if needed
   */
  public void dumpContainerLogs(File errorFile) throws ContainerException;

}
