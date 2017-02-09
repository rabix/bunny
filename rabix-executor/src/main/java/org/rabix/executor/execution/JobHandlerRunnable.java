package org.rabix.executor.execution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rabix.executor.execution.JobHandlerCommand.Repeat;
import org.rabix.executor.handler.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job execution thread runnable. It executes commands one by one in synchronous matter. 
 */
public class JobHandlerRunnable implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(JobHandlerRunnable.class);

  private final static long DEFAULT_SLEEP_TIME = TimeUnit.SECONDS.toMillis(1);

  private final String jobId;
  private final String contextId;
  private final JobHandler jobHandler;
  private final BlockingQueue<JobHandlerCommand> commands;

  private final AtomicBoolean stop = new AtomicBoolean(false);

  public JobHandlerRunnable(String jobId, String contextId, JobHandler jobHandler) {
    this.jobId = jobId;
    this.contextId = contextId;
    this.jobHandler = jobHandler;
    this.commands = new LinkedBlockingQueue<>();
  }

  @Override
  public void run() {
    logger.info("JobHandlerRunnable {} started.", Thread.currentThread().getName());

    long sleepTime = DEFAULT_SLEEP_TIME;
    
    while (!isStopped()) {
      try {
        JobHandlerCommand command = commands.poll();
        if (command == null) {
          logger.debug("No active commands. Sleep for {}", sleepTime);
          Thread.sleep(sleepTime);
          continue;
        }
        logger.debug("Command {} found. Start execution.", command);

        Repeat repeat = command.getRepeat();
        if (repeat != null) {
          logger.debug("Command {} is repeatable. Delay and put it back to queue.", command);
          Thread.sleep(repeat.delay);
          addCommand(command);
        }

        JobHandlerCommand.Result result = command.run(jobId, contextId, jobHandler);
        if (result.isLastCommand) {
          logger.debug("Command {} is last command. Stop thread.", command);
          stop();
        }
      } catch (Exception e) {
        logger.error("JobHandlerRunnable faced a runtime error. Stop execution.", e);
        stop();
      }
    }
    logger.info("JobHandlerRunnable {} finished.", Thread.currentThread().getName());
  }

  /**
   * Add command to queue 
   */
  public void addCommand(JobHandlerCommand command) {
    if (stop.get()) {
      logger.error("Failed to add command {}. Thread is stopped.", command);
    }
    this.commands.add(command);
  }

  /**
   * Stop runnable
   */
  public void stop() {
    stop.set(true);
    logger.info("JobHandlerRunnable {} stopped.", Thread.currentThread().getName());
  }

  /**
   * Is runnable stopped? 
   */
  public boolean isStopped() {
    return stop.get();
  }

}
