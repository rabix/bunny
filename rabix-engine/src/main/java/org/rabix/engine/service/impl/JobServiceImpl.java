package org.rabix.engine.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.AppDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.JobRepository.JobEntity;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.impl.JobRecordService.JobState;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.validator.JobStateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobServiceImpl implements JobService {

  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;
  
  private final JobRepository jobRepository;
  private final DAGNodeDB dagNodeDB;
  private final AppDB appDB;
  
  private final EventProcessor eventProcessor;
  private final SchedulerService scheduler;
  
  private final TransactionHelper transactionHelper;
  
  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, SchedulerService scheduler, DAGNodeDB dagNodeDB, AppDB appDB, JobRepository jobRepository, TransactionHelper transactionHelper, EngineStatusCallback statusCallback) {
    this.dagNodeDB = dagNodeDB;
    this.appDB = appDB;
    this.eventProcessor = eventProcessor;
    this.jobRepository = jobRepository;
    
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
    this.scheduler = scheduler;
    this.transactionHelper = transactionHelper;
    
    this.eventProcessor.start(statusCallback);
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
          UUID backendId = jobRepository.getBackendId(job.getId());
          if (backendId == null) {
            logger.warn("Tried to update Job " + job.getId() + " without backend assigned.");
            return null;
          }
          JobStatus dbStatus = jobRepository.getStatus(job.getId());
          JobStateValidator.checkState(JobHelper.transformStatus(dbStatus), JobHelper.transformStatus(job.getStatus()));

          JobRecord jobRecord = jobRecordService.find(job.getName(), job.getRootId());
          if (jobRecord == null) {
            logger.info("Possible stale message. Job {} for root {} doesn't exist.", job.getName(), job.getRootId());
            return null;
          }
          JobStatusEvent statusEvent = null;
          JobStatus status = job.getStatus();
          switch (status) {
          case RUNNING:
            if (JobState.RUNNING.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobState.RUNNING);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.RUNNING, job.getOutputs(), job.getId(), job.getName());
            break;
          case FAILED:
            if (JobState.FAILED.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobState.FAILED);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.FAILED, null, job.getId(), job.getName());
            break;
          case COMPLETED:
            if (JobState.COMPLETED.equals(jobRecord.getState())) {
              return null;
            }
            JobStateValidator.checkState(jobRecord, JobState.COMPLETED);
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.COMPLETED, job.getOutputs(), job.getId(), job.getName());
            break;
          default:
            break;
          }
          jobRepository.update(job);
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
          UUID rootId = UUID.randomUUID();
          Job updatedJob = Job.cloneWithIds(job, rootId, rootId);
          updatedJob = Job.cloneWithName(updatedJob, InternalSchemaHelper.ROOT_NAME);

          Bindings bindings = null;
          bindings = BindingsFactory.create(updatedJob);

          DAGNode node = bindings.translateToDAG(updatedJob);
          appDB.loadDB(node);
          String dagHash = dagNodeDB.loadDB(node, rootId);

          updatedJob = Job.cloneWithStatus(updatedJob, JobStatus.RUNNING);
          updatedJob = Job.cloneWithConfig(updatedJob, config);
          jobRepository.insert(updatedJob, null, null);

          InitEvent initEvent = new InitEvent(rootId, updatedJob.getInputs(), updatedJob.getRootId(), updatedJob.getConfig(), dagHash, null);
          eventProcessor.persist(initEvent);
          eventWrapper.set(initEvent);
          jobWrapper.set(updatedJob);
          isSuccessful.set(true);
          return null;
        }
      });
      if (isSuccessful.get()) {
        eventProcessor.addToExternalQueue(eventWrapper.get());
        return jobWrapper.get();
      }
      return job;
    } catch (Exception e) {
      logger.error("Failed to create Bindings", e);
      throw new JobServiceException("Failed to create Bindings", e);
    }
  }
  
  @Override
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
  public Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException {
    return JobHelper.createReadyJobs(jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, appDB, rootId);
  }
  
  @Override
  public Set<Job> get() {
    return jobRepository.get();
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

}
