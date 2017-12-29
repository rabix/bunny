package org.rabix.executor.container;

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
   * Get container exit message 
   */
  public String getProcessExitMessage() throws ContainerException;

  /**
   * Dumps command line into a file 
   */
  public void dumpCommandLine() throws ContainerException;

  /**
   * Remove container
   */
  public void removeContainer();

}
