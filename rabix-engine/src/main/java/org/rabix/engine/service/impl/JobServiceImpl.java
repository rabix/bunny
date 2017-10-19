package org.rabix.engine.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.AppService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobServiceException;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.repository.JobRepository;
import org.rabix.engine.store.repository.JobRepository.JobEntity;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.validator.JobStateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobServiceImpl implements JobService {

  private static final long FREE_RESOURCES_WAIT_TIME = 3000L;
  
  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;

  private final JobRepository jobRepository;
  private final DAGNodeService dagNodeService;
  private final AppService appService;
  
  private final EventProcessor eventProcessor;
  private final SchedulerService scheduler;
  
  private final TransactionHelper transactionHelper;

  private boolean deleteFilesUponExecution;
  private boolean deleteIntermediaryFiles;
  private boolean keepInputFiles;
  private boolean isLocalBackend;

  private IntermediaryFilesService intermediaryFilesService;
  
  private Set<UUID> stoppingRootIds = new HashSet<>();
  private EngineStatusCallback engineStatusCallback;
  private boolean setResources;

  private JobHelper jobHelper;
  
  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService,
      VariableRecordService variableRecordService, LinkRecordService linkRecordService,
      ContextRecordService contextRecordService, SchedulerService scheduler, DAGNodeService dagNodeService,
      AppService appService, JobRepository jobRepository, TransactionHelper transactionHelper,
      EngineStatusCallback statusCallback, Configuration configuration,
      IntermediaryFilesService intermediaryFilesService, JobHelper jobHelper) {
    this.dagNodeService = dagNodeService;
    this.appService = appService;
    this.eventProcessor = eventProcessor;
    this.jobRepository = jobRepository;

    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
    this.scheduler = scheduler;
    this.transactionHelper = transactionHelper;
    this.engineStatusCallback = statusCallback;
    this.intermediaryFilesService = intermediaryFilesService;
    this.jobHelper = jobHelper;

    deleteIntermediaryFiles = configuration.getBoolean("engine.delete_intermediary_files", false);
    keepInputFiles = !configuration.getBoolean("engine.treat_inputs_as_intermediary", false) || !deleteIntermediaryFiles;
    setResources = configuration.getBoolean("engine.set_resources", false);
  }
  
  @Override
  public void update(Job job) throws JobServiceException {
    logger.debug("Update Job {}", job.getId());
    try {
      final AtomicBoolean isSuccessful = new AtomicBoolean(false);
      final AtomicReference<Event> eventWrapper = new AtomicReference<Event>(null);
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          if (!job.isRoot()) {
            UUID backendId = jobRepository.getBackendId(job.getId());
            if (backendId == null) {
              logger.warn("Tried to update Job " + job.getId() + " without backend assigned.");
              return null;
            }  
            
            JobStatus dbStatus = jobRepository.getStatus(job.getId());
            JobStateValidator.checkState(JobHelper.transformStatus(dbStatus), JobHelper.transformStatus(job.getStatus()));
          }
          JobRecord jobRecord = jobRecordService.find(job.getName(), job.getRootId());
          if (jobRecord == null) {
            logger.info("Possible stale message. Job {} for root {} doesn't exist.", job.getName(), job.getRootId());
            return null;
          }
          JobStatusEvent statusEvent = null;
          JobStatus status = job.getStatus();
          switch (status) {
          case RUNNING:
            if (JobRecord.JobState.RUNNING.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobRecord.JobState.RUNNING);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.RUNNING, job.getOutputs(), job.getId(), job.getName());
            break;
          case FAILED:
            if (JobRecord.JobState.FAILED.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobRecord.JobState.FAILED);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.FAILED, job.getMessage(), job.getId(), job.getName());
            break;
          case ABORTED:
            if (JobRecord.JobState.ABORTED.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobRecord.JobState.ABORTED);
            Job rootJob = jobRepository.get(jobRecord.getRootId());
            handleJobRootAborted(rootJob);
            statusEvent = new JobStatusEvent(rootJob.getName(), rootJob.getRootId(), JobRecord.JobState.ABORTED, rootJob.getId(), rootJob.getName());
            break;
          case COMPLETED:
            if (JobRecord.JobState.COMPLETED.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobRecord.JobState.COMPLETED);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.COMPLETED, job.getOutputs(), job.getId(), job.getName());
            break;
          default:
            break;
          }
          jobRepository.updatePartial(job);
          eventProcessor.persist(statusEvent);
          eventWrapper.set(statusEvent);
          isSuccessful.set(true);
          return null;
        }
      });
      if (isSuccessful.get()) {
        eventProcessor.addToExternalQueue(eventWrapper.get());
      }
    } catch (Exception e) {
      // TODO handle exception
      logger.error("Failed to update Job " + job.getName() + " and root ID " + job.getRootId(), e);
    }
  }
  
  @Override
  public Job start(final Job job, Map<String, Object> config) throws JobServiceException {
    logger.debug("Start Job {}", job);
    try {
      final AtomicReference<Job> jobWrapper = new AtomicReference<Job>(job);
      final AtomicReference<Event> eventWrapper = new AtomicReference<Event>(null);
      final AtomicBoolean isSuccessful = new AtomicBoolean(false);
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          UUID rootId = job.getRootId();
          if (rootId == null)
            rootId = UUID.randomUUID();
          
          Job updatedJob = Job.cloneWithIds(job, rootId, rootId);
          updatedJob = Job.cloneWithName(updatedJob, InternalSchemaHelper.ROOT_NAME);

          Bindings bindings = null;
          bindings = BindingsFactory.create(updatedJob);

          DAGNode node = bindings.translateToDAG(updatedJob);
          appService.loadDB(node);
          String dagHash = dagNodeService.put(node, rootId);

          
          updatedJob = Job.cloneWithStatus(updatedJob, JobStatus.RUNNING);
          updatedJob = Job.cloneWithConfig(updatedJob, config);
          jobRepository.insert(updatedJob, null, null);

          InitEvent initEvent = new InitEvent(rootId, updatedJob.getInputs(), updatedJob.getRootId(), updatedJob.getConfig(), dagHash, InternalSchemaHelper.ROOT_NAME);
          eventProcessor.persist(initEvent);
          eventWrapper.set(initEvent);
          jobWrapper.set(updatedJob);
          isSuccessful.set(true);
          return null;
        }
      });
      logger.info("Job {} rootId: {} started", job.getName(), job.getRootId());
      if (isSuccessful.get()) {
        eventProcessor.addToExternalQueue(eventWrapper.get());
        return jobWrapper.get();
      }
      return job;
    } catch (Exception e) {
      throw new JobServiceException("Failed to create Bindings", e);
    }
  }
  
  public void stop(Job job) throws JobServiceException {
    logger.debug("Stop Job {}", job.getId());
    
    if (job.isRoot()) {
      Set<JobStatus> statuses = new HashSet<>();
      statuses.add(JobStatus.READY);
      statuses.add(JobStatus.PENDING);
      statuses.add(JobStatus.RUNNING);
      statuses.add(JobStatus.STARTED);
      jobRepository.updateStatus(job.getId(), JobStatus.ABORTED, statuses);

      Set<Job> jobs = jobRepository.getByRootId(job.getRootId());
      scheduler.stop(jobs.toArray(new Job[jobs.size()]));
    } else {
      // TODO implement the rest of the logic
      scheduler.stop(job);
    }
    logger.info("Job {} rootId: {} stopped", job.getName(), job.getRootId());
  }
  
  @Override
  public void stop(UUID id) throws JobServiceException {
    Job job = jobRepository.get(id);
    stop(job);
  }
  
  @Override
  public Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException {
    return jobHelper.createReadyJobs(rootId, setResources);
  }
  
  @Override
  public Job get(UUID id) {
    return jobRepository.get(id);
  }
  
  public void delete(UUID jobId) {
    // TODO think about it
  }

  public void updateBackend(UUID jobId, UUID backendId) {
    this.jobRepository.updateBackendId(jobId, backendId);
  }
  
  @Override
  public void updateBackends(Set<JobEntity> entities) {
    this.jobRepository.updateBackendIds(entities.iterator());
  }

  public Set<UUID> getBackendsByRootId(UUID rootId) {
    return jobRepository.getBackendsByRootId(rootId);
  }

  public void dealocateJobs(UUID backendId) {
    jobRepository.dealocateJobs(backendId);
  }
  
  public Set<JobEntity> getReadyFree() {
    return jobRepository.getReadyFree();
  }

  @Override
  public void handleJobsReady(Set<Job> jobs, UUID rootId, String producedByNode){
    try {
      engineStatusCallback.onJobsReady(jobs, rootId, producedByNode);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobFailed(final Job failedJob){
    logger.warn("Job {}, rootId: {} failed: {}", failedJob.getName(), failedJob.getRootId(), failedJob.getMessage());

    if (deleteIntermediaryFiles) {
      intermediaryFilesService.handleJobFailed(failedJob, jobRepository.get(failedJob.getRootId()), keepInputFiles);
    }
    try {
      engineStatusCallback.onJobFailed(failedJob);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobContainerReady(Job containerJob) {
    try {
      engineStatusCallback.onJobContainerReady(containerJob);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobRootCompleted(Job job){
    logger.info("Root job {} completed.", job.getId());
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
    job = jobHelper.fillOutputs(job);
    jobRepository.update(job);
    try {
      engineStatusCallback.onJobRootCompleted(job);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobRootFailed(Job job){
    logger.warn("Root job {} failed.", job.getId());
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
    try {
      engineStatusCallback.onJobRootFailed(job);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobRootPartiallyCompleted(UUID rootId, Map<String, Object> outputs, String producedBy){
    logger.info("Root {} is partially completed.", rootId);
    try{
      engineStatusCallback.onJobRootPartiallyCompleted(rootId, outputs, producedBy);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed",e);
    }
  }
  
  @Override
  public void handleJobRootAborted(Job rootJob) {
    logger.info("Root {} has been aborted", rootJob.getId());

    try {
      stop(rootJob);
    } catch (JobServiceException e) {
      logger.error("Failed to stop jobs", e);
    }
    try {
      engineStatusCallback.onJobRootAborted(rootJob);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobCompleted(Job job){
    logger.info("Job {} rootId: {} is completed.", job.getName(), job.getRootId());
    if (deleteIntermediaryFiles) {
      intermediaryFilesService.handleJobCompleted(job);
    }
    try{
      engineStatusCallback.onJobCompleted(job);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed",e);
    }
  }
}
