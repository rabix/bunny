package org.rabix.executor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubActiveMQ;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.api.engine.EngineStubRabbitMQ;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class WorkerServiceImpl implements WorkerService {

  private static final Logger logger = LoggerFactory.getLogger(WorkerServiceImpl.class);

  private final JobDataService jobDataService;

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private Configuration configuration;
  private EngineStub<?,?,?> engineStub;
  
  @Inject
  public WorkerServiceImpl(JobDataService jobDataService, Configuration configuration) {
    this.configuration = configuration;
    this.jobDataService = jobDataService;
  }

  @Override
  public void initialize(Backend backend) {
    try {
      switch (backend.getType()) {
      case LOCAL:
        engineStub = new EngineStubLocal((BackendLocal) backend, this, configuration);
        break;
      case RABBIT_MQ:
        engineStub = new EngineStubRabbitMQ((BackendRabbitMQ) backend, this, configuration);
        break;
      case ACTIVE_MQ:
        engineStub = new EngineStubActiveMQ((BackendActiveMQ) backend, this, configuration);
      default:
        break;
      }
      jobDataService.initialize(engineStub);
      engineStub.start();
    } catch (TransportPluginException e) {
      logger.error("Failed to initialize Executor", e);
      throw new RuntimeException("Failed to initialize Executor", e);
    }
  }

  @Override
  public void start(final Job job, UUID rootId) {
    logger.debug("start(id={}, important={}, uploadOutputs={})", job.getId());

    final JobData jobData = new JobData(job, JobDataStatus.PENDING, "Job is queued to start.", false, false);
    jobDataService.save(jobData);
  }

  @Override
  public void stop(List<UUID> ids, UUID rootId) {
    logger.debug("stop(ids={})", ids);

    for (UUID id : ids) {
      final JobData jobData = jobDataService.find(id, rootId);
      if (!isFinished(jobData.getStatus())) {
        jobDataService.save(jobData, "Stopping job", JobDataStatus.ABORTING);
      }
    }
  }

  @Override
  public void free(UUID rootId, Map<String, Object> config) {
    // free resources
  }
  
  @Override
  public JobStatus findStatus(UUID jobId, UUID rootId) {
    logger.debug("findStatus(id={})", jobId);

    JobData jobData = jobDataService.find(jobId, rootId);
    if (jobData != null) {
      return JobDataStatus.convertToJobStatus(jobData.getStatus());
    }
    return null;
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    logger.debug("shutdown(stopEverything={})", stopEverything);

    List<JobData> jobsToStop = jobDataService.find(JobDataStatus.STARTED, JobDataStatus.READY);

    int abortedJobsCount = 0;
    if (jobsToStop != null) {
      for (JobData jobData : jobsToStop) {
        if (!stopEverything && jobData.isImportant()) {
          continue;
        }
        jobDataService.save(jobData, "Stopping job", JobDataStatus.ABORTING);
        abortedJobsCount++;
      }
    }
    stopped.set(true);
    String message = String.format("Shutdown%s executed. Worker has stopped %d %s.", stopEverything ? " now" : "", abortedJobsCount, abortedJobsCount == 1 ? "job" : "jobs");
    logger.info(message);
  }

  @Override
  public Map<String, Object> getResult(UUID id, UUID rootId) {
    JobData jobData = jobDataService.find(id, rootId);
    return jobData.getResult();
  }

  @Override
  public boolean isRunning(UUID id, UUID rootId) {
    logger.debug("isRunning(id={})", id);

    JobData jobData = jobDataService.find(id, rootId);
    if (jobData != null && !isFinished(jobData.getStatus())) {
      logger.info("Command line tool {} is running. The status is {}", id, jobData.getStatus());
      return true;
    }
    logger.info("Command line tool {} is not running", id);
    return false;
  }

  private boolean isFinished(JobDataStatus jobStatus) {
    switch (jobStatus) {
    case COMPLETED:
    case FAILED:
    case ABORTED:
    case ABORTING:
      return true;
    default:
      return false;
    }
  }

  @Override
  public boolean isStopped() {
    return stopped.get();

  }

}
