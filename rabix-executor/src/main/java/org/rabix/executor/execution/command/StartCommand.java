package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.bindings.model.Job;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.JobHandlerCommand;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.status.ExecutorStatusCallback;

/**
 * Command that starts {@link JobHandler}
 */
public class StartCommand extends JobHandlerCommand {

  @Inject
  public StartCommand(JobDataService jobDataService, ExecutorStatusCallback statusCallback) {
    super(jobDataService, statusCallback);
  }

  @Override
  public Result run(JobData data, JobHandler handler, String contextId) {
    Job job = data.getJob();
    try {
      handler.start();
      data = jobDataService.save(data, "Job " + job.getId() + " started successfully.", JobDataStatus.STARTED);
      started(data, "Job " + job.getId() + " started successfully.", handler.getEngineStub());
    } catch (ExecutorException e) {
      String message = String.format("Failed to start %s. %s", job.getId(), e.toString());
      data = jobDataService.save(data, message, JobDataStatus.FAILED);
      failed(data, message, handler.getEngineStub(), e);
      return new Result(true);
    }
    return new Result(false);
  }

  @Override
  public JobHandlerCommandType getType() {
    return JobHandlerCommandType.START;
  }

}
