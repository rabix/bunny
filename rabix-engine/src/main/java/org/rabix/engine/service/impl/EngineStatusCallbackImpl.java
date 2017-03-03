package org.rabix.engine.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.common.SystemEnvironmentHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.AppDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.helper.IntermediaryFilesHelper;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class EngineStatusCallbackImpl implements EngineStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;

  private final JobRepository jobRepository;
  private final SchedulerService scheduler;
  private final IntermediaryFilesService intermediaryFilesService;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private boolean isLocalBackend;
  private boolean deleteFilesUponExecution;
  private boolean deleteIntermediaryFiles;
  private boolean keepInputFiles;

  private boolean stopOnFail;
  private boolean setResources;

  private static final long FREE_RESOURCES_WAIT_TIME = 3000L;

  private Set<UUID> stoppingRootIds = new HashSet<>();

  @Inject
  public EngineStatusCallbackImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService,
      LinkRecordService linkRecordService, ContextRecordService contextRecordService, SchedulerService scheduler,
      IntermediaryFilesService intermediaryFilesService, Configuration configuration, DAGNodeDB dagNodeDB, AppDB appDB, JobRepository jobRepository, JobService jobService) {
    
    this.jobRepository = jobRepository;

    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.scheduler = scheduler;

    this.intermediaryFilesService = intermediaryFilesService;
    
    deleteFilesUponExecution = configuration.getBoolean("rabix.delete_files_upon_execution", false);
    deleteIntermediaryFiles = configuration.getBoolean("rabix.delete_intermediary_files", false);
    keepInputFiles = configuration.getBoolean("rabix.keep_input_files", true);
    isLocalBackend = configuration.getBoolean("local.backend", false);
  }

  @Override
  public void onJobReady(Job job) {
    if (setResources) {
      long numberOfCores;
      long memory;
      if (job.getConfig() != null) {
        numberOfCores = job.getConfig().get("allocatedResources.cpu") != null ? Long.parseLong((String) job.getConfig().get("allocatedResources.cpu")) : SystemEnvironmentHelper.getNumberOfCores();
        memory = job.getConfig().get("allocatedResources.mem") != null ? Long.parseLong((String) job.getConfig().get("allocatedResources.mem")) : SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
      } else {
        numberOfCores = SystemEnvironmentHelper.getNumberOfCores();
        memory = SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
      }
      Resources resources = new Resources(numberOfCores, memory, null, true, null, null, null, null);
      job = Job.cloneWithResources(job, resources);
      jobRepository.update(job);
    }
  }

  @Override
  public void onJobsReady(Set<Job> jobs) throws EngineStatusCallbackException {
    for (Job job : jobs) {
      onJobReady(job);
    }
  }

  public void stop(UUID id) throws JobServiceException {
    logger.debug("Stop Job {}", id);
    
    Job job = jobRepository.get(id);
    if (job.isRoot()) {
      Set<Job> jobs = jobRepository.getByRootId(job.getRootId());
      scheduler.stop(jobs.toArray(new Job[jobs.size()]));
    } else {
      scheduler.stop(job);
    }
  }
  
  @Override
  public void onJobFailed(final Job failedJob) throws EngineStatusCallbackException {
    if (stopOnFail) {
      synchronized (stoppingRootIds) {
        if (stoppingRootIds.contains(failedJob.getRootId())) {
          return;
        }
        stoppingRootIds.add(failedJob.getRootId());

        try {
          stop(failedJob.getRootId());
        } catch (JobServiceException e) {
          logger.error("Failed to stop Root job " + failedJob.getRootId(), e);
        }
        executorService.submit(new Runnable() {
          @Override
          public void run() {
            while (true) {
              try {
                boolean exit = true;
                for (Job job : jobRepository.getByRootId(failedJob.getRootId())) {
                  if (!job.isRoot() && !isFinished(job.getStatus())) {
                    exit = false;
                    break;
                  }
                }
                if (exit) {
                  onJobRootFailed(failedJob);
                  break;
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
              } catch (Exception e) {
                logger.error("Failed to stop root Job " + failedJob.getRootId(), e);
                break;
              }
            }
          }
        });
      }
    }
    if (deleteIntermediaryFiles) {
      IntermediaryFilesHelper.handleJobFailed(failedJob, jobRepository.get(failedJob.getRootId()), intermediaryFilesService, keepInputFiles);
    }
  }

  private boolean isFinished(JobStatus jobStatus) {
    switch (jobStatus) {
      case COMPLETED:
      case FAILED:
      case ABORTED:
        return true;
      default:
        return false;
    }
  }

  public void onJobContainerReady(Job containerJob) {
    if (deleteIntermediaryFiles) {
      IntermediaryFilesHelper.handleContainerReady(containerJob, linkRecordService, intermediaryFilesService, keepInputFiles);
    }
  }

  @Override
  public void onJobRootCompleted(Job job) throws EngineStatusCallbackException {
    if (deleteFilesUponExecution) {
      scheduler.freeBackend(job);

      if (isLocalBackend) {
        try {
          Thread.sleep(FREE_RESOURCES_WAIT_TIME);
        } catch (InterruptedException e) {
        }
      }
    }

    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    job = JobHelper.fillOutputs(job, jobRecordService, variableRecordService);
    jobRepository.update(job);
  }

  @Override
  public void onJobRootFailed(Job job) throws EngineStatusCallbackException {
    synchronized (stoppingRootIds) {
      if (deleteFilesUponExecution) {
        scheduler.freeBackend(job);

        if (isLocalBackend) {
          try {
            Thread.sleep(FREE_RESOURCES_WAIT_TIME);
          } catch (InterruptedException e) {
          }
        }
      }

      job = Job.cloneWithStatus(job, JobStatus.FAILED);
      jobRepository.update(job);

      scheduler.deallocate(job);
      stoppingRootIds.remove(job.getId());
    }
  }

  @Override
  public void onJobRootPartiallyCompleted(Job rootJob) throws EngineStatusCallbackException {
    logger.info("Root {} is partially completed.", rootJob.getId());
  }

  @Override
  public void onJobCompleted(Job job) throws EngineStatusCallbackException {
    logger.info("Job {} is completed.", job.getName());
    if (deleteIntermediaryFiles) {
      IntermediaryFilesHelper.handleJobCompleted(job, linkRecordService, intermediaryFilesService);
    }
  }
}
