package org.rabix.executor.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.BindingException;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.execution.command.StartCommand;
import org.rabix.executor.execution.command.StatusCommand;
import org.rabix.executor.execution.command.StopCommand;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JobDataServiceImpl implements JobDataService {

  private final static Logger logger = LoggerFactory.getLogger(JobDataServiceImpl.class);

  private final Map<String, Map<String, JobData>> jobDataMap = new HashMap<>();

  private Provider<StopCommand> stopCommandProvider;
  private Provider<StartCommand> startCommandProvider;
  private Provider<StatusCommand> statusCommandProvider;
  
  private JobHandlerCommandDispatcher jobHandlerCommandDispatcher;

  private EngineStub<?,?,?> engineStub;
  
  private ScheduledExecutorService starter = Executors.newSingleThreadScheduledExecutor();

  private JobFitter jobFitter;

  @Inject
  public JobDataServiceImpl(JobHandlerCommandDispatcher jobHandlerCommandDispatcher,
      Provider<StopCommand> stopCommandProvider, Provider<StartCommand> startCommandProvider,
      Provider<StatusCommand> statusCommandProvider, JobFitter jobFitter) {
    this.jobFitter = jobFitter;
    this.jobHandlerCommandDispatcher = jobHandlerCommandDispatcher;
    this.stopCommandProvider = stopCommandProvider;
    this.startCommandProvider = startCommandProvider;
    this.statusCommandProvider = statusCommandProvider;
  }
  
  @Override
  public void initialize(EngineStub<?,?,?> engineStub) {
    this.engineStub = engineStub;
    this.starter.scheduleAtFixedRate(new JobStatusHandler(), 0, 100, TimeUnit.MILLISECONDS);
  }
  
  @Override
  public JobData find(String id, String contextId) {
    Preconditions.checkNotNull(id);
    synchronized (jobDataMap) {
      return getJobDataMap(contextId).get(id);
    }
  }

  @Override
  public List<JobData> find(JobDataStatus... statuses) {
    Preconditions.checkNotNull(statuses);

    synchronized (jobDataMap) {
      List<JobDataStatus> statusList = Arrays.asList(statuses);
      List<JobData> jobDataByStatus = new ArrayList<>();
      for (Entry<String, Map<String, JobData>> entry : jobDataMap.entrySet()) {
        for (JobData jobData : entry.getValue().values()) {
          if (statusList.contains(jobData.getStatus())) {
            jobDataByStatus.add(jobData);
          }
        }
      }
      return jobDataByStatus;
    }
  }

  @Override
  public void save(JobData jobData) {
    Preconditions.checkNotNull(jobData);
    synchronized (jobDataMap) {
      getJobDataMap(jobData.getJob().getRootId()).put(jobData.getId(), jobData);
    }
  }
  
  @Override
  public JobData save(JobData jobData, String message, JobDataStatus status) {
    Preconditions.checkNotNull(jobData);
    synchronized (jobDataMap) {
      jobData = JobData.cloneWithStatusAndMessage(jobData, status, message);
      save(jobData);
      return jobData;
    }
  }
  
  private Map<String, JobData> getJobDataMap(String contextId) {
    synchronized (jobDataMap) {
      Map<String, JobData> jobList = jobDataMap.get(contextId);
      if (jobList == null) {
        jobList = new HashMap<>();
        jobDataMap.put(contextId, jobList);
      }
      return jobList;
    }
  }
  
  private class JobStatusHandler implements Runnable {
    @Override
    public void run() {
      synchronized (jobDataMap) {
        List<JobData> aborting = find(JobDataStatus.ABORTING);
        for (JobData jobData : aborting) {
          save(JobData.cloneWithStatus(jobData, JobDataStatus.ABORTED));
          jobHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get(), engineStub);
        }

        List<JobData> pending = find(JobDataStatus.PENDING);

        JobData jobData = null;
        for (int i = 0; i < pending.size(); i++) {
          try {
            jobData = pending.get(i);
            if (!jobFitter.tryToFit(jobData.getJob())) {
              continue;
            }
            save(JobData.cloneWithStatus(jobData, JobDataStatus.READY));

            jobHandlerCommandDispatcher.dispatch(jobData, startCommandProvider.get(), engineStub);
            jobHandlerCommandDispatcher.dispatch(jobData, statusCommandProvider.get(), engineStub);
          } catch (BindingException e) {
            logger.error("Failed to schedule Job " + jobData.getId() + " for execution.", e);
          }
        }
      }
    }
  }

}
