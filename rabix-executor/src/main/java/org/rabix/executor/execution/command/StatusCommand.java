package org.rabix.executor.execution.command;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.rabix.bindings.model.Job;
import org.rabix.executor.execution.JobHandlerCommand;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that checks status of {@link JobHandler} 
 */
public class StatusCommand extends JobHandlerCommand {

  private final static Logger logger = LoggerFactory.getLogger(StatusCommand.class);

  public final static long DEFAULT_DELAY = TimeUnit.SECONDS.toMillis(1);
  
  private JobFitter jobFitter;
  
  @Inject
  public StatusCommand(JobDataService jobDataService, ExecutorStatusCallback statusCallback, JobFitter jobFitter) {
    super(jobDataService, statusCallback);
    this.jobFitter = jobFitter;
  }

  @Override
  public Result run(JobData jobData, JobHandler jobHandler, String contextId) {
    String jobId = jobData.getJob().getId();
    logger.debug("Check status for {} command line tool.", jobId);

    if (!JobDataStatus.STARTED.equals(jobData.getStatus())) {
      logger.info("Command line tool {} is not started yet.", jobId);
      return new Result(false);
    }
    try {
      Job job = jobData.getJob();
      if (jobHandler.isRunning()) {
        logger.info("Command line tool {} for context {} is still running.", job.getId(), job.getRootId());
        return new Result(false);
      }
      
      String message = null;
      job = jobHandler.postprocess(jobData.isTerminal());
      if (!jobHandler.isSuccessful()) {
        message = String.format("Job %s failed with exit code %d.", job.getId(), jobHandler.getExitStatus());
        jobData = jobDataService.save(jobData, message, JobDataStatus.FAILED);
        failed(jobData, message, jobHandler.getEngineStub(), null);
      } else {
        message = String.format("Job %s completed successfully.", job.getId());
        jobData = jobDataService.save(jobData, message, JobDataStatus.COMPLETED);
        completed(jobData, message, job.getOutputs(), jobHandler.getEngineStub());
      }
      jobFitter.free(job);
    } catch (Exception e) {
      String message = String.format("Failed to execute status command for %s. %s", jobId, e.getMessage());
      jobData = jobDataService.save(jobData, message, JobDataStatus.FAILED);
      failed(jobData, message, jobHandler.getEngineStub(), e);
      return new Result(true);
    }
    return new Result(true);
  }

  @Override
  public Repeat getRepeat() {
    return new Repeat(DEFAULT_DELAY);
  }

  @Override
  public JobHandlerCommandType getType() {
    return JobHandlerCommandType.STATUS;
  }

}
