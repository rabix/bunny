package org.rabix.engine.service.impl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
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
import org.rabix.engine.metrics.MetricsHelper;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.*;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.repository.JobRepository;
import org.rabix.engine.store.repository.JobRepository.JobEntity;
import org.rabix.engine.store.repository.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class JobServiceImpl implements JobService {

  private static final long FREE_RESOURCES_WAIT_TIME = 3000L;

  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
  private final JobRepository jobRepository;

  private final DAGNodeService dagNodeService;
  private final AppService appService;

  private final EventProcessor eventProcessor;
  private final TransactionHelper transactionHelper;
  private final MetricsHelper metricsHelper;
  private final GarbageCollectionService garbageCollectionService;

  private boolean deleteFilesUponExecution;
  private boolean isLocalBackend;

  private IntermediaryFilesService intermediaryFilesService;

  private Set<UUID> stoppingRootIds = new HashSet<>();
  private EngineStatusCallback engineStatusCallback;
  private boolean setResources;

  private JobHelper jobHelper;

  @Inject
  public JobServiceImpl(EventProcessor eventProcessor,
                        DAGNodeService dagNodeService,
                        AppService appService,
                        JobRepository jobRepository,
                        TransactionHelper transactionHelper,
                        EngineStatusCallback statusCallback,
                        Configuration configuration,
                        IntermediaryFilesService intermediaryFilesService,
                        JobHelper jobHelper,
                        MetricsHelper metricsHelper,
                        GarbageCollectionService garbageCollectionService) {
    this.dagNodeService = dagNodeService;
    this.appService = appService;
    this.eventProcessor = eventProcessor;
    this.jobRepository = jobRepository;
    this.transactionHelper = transactionHelper;
    this.engineStatusCallback = statusCallback;
    this.intermediaryFilesService = intermediaryFilesService;
    this.jobHelper = jobHelper;
    this.metricsHelper = metricsHelper;
    this.garbageCollectionService = garbageCollectionService;

    setResources = configuration.getBoolean("engine.set_resources", false);
  }

  @Override
  public void update(Job job) throws JobServiceException {
    metricsHelper.time(() -> doUpdate(job, null), "JobServiceImpl.update");
  }

  @Override
  public void update(Job job, Runnable onUpdatedCallback) throws JobServiceException {
    metricsHelper.time(() -> doUpdate(job, onUpdatedCallback), "JobServiceImpl.update");
  }

  private void doUpdate(Job job, Runnable onUpdatedCallback) {
    logger.debug("Update job id:{}, name:{}, root:{}", job.getId(), job.getName(), job.getRootId());
    try {
      transactionHelper.doInTransaction((TransactionHelper.TransactionCallback<Void>) () -> {
        JobStatusEvent statusEvent = null;
        JobStatus status = job.getStatus();

        switch (status) {
          case RUNNING:
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.RUNNING, job.getOutputs(), job.getId(), job.getName());
            break;
          case FAILED:
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.FAILED, job.getMessage(), job.getId(), job.getName());
            break;
          case ABORTED:
            Job rootJob = jobRepository.get(job.getRootId());
            handleJobRootAborted(rootJob);
            statusEvent = new JobStatusEvent(rootJob.getName(), rootJob.getRootId(), JobRecord.JobState.ABORTED, rootJob.getId(), rootJob.getName());
            break;
          case COMPLETED:
            statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobRecord.JobState.COMPLETED, job.getOutputs(), job.getId(), job.getName());
            break;
          default:
            break;
        }
        eventProcessor.addToExternalQueue(statusEvent, onUpdatedCallback);
        return null;
      });
    } catch (Exception e) {
      // TODO handle exception
      logger.error("Failed to update Job " + job.getName() + " and root ID " + job.getRootId(), e);
    }
  }

  @Override
  public Job start(final Job job, Map<String, Object> config) throws JobServiceException {
    logger.debug("Start Job {}", job);
    try {
      final AtomicReference<Job> jobWrapper = new AtomicReference<>(job);
      final AtomicReference<Event> eventWrapper = new AtomicReference<>(null);

      final AtomicBoolean isSuccessful = new AtomicBoolean(false);
      transactionHelper.doInTransaction((TransactionHelper.TransactionCallback<Void>) () -> {
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

        updatedJob = Job.cloneWithStatus(updatedJob, JobStatus.PENDING);
        updatedJob = Job.cloneWithConfig(updatedJob, config);
        jobRepository.insert(updatedJob, updatedJob.getRootId(), null);

        InitEvent initEvent = new InitEvent(rootId, updatedJob.getInputs(), updatedJob.getRootId(), updatedJob.getConfig(), dagHash, InternalSchemaHelper.ROOT_NAME);
        eventProcessor.persist(initEvent);

        eventWrapper.set(initEvent);
        jobWrapper.set(updatedJob);
        isSuccessful.set(true);
        return null;
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
    }
    logger.info("Job {} rootId: {} stopped", job.getName(), job.getRootId());
  }

  @Override
  public void stop(UUID id) throws JobServiceException {
    Job job = jobRepository.get(id);
    if (job != null) {
      job = Job.cloneWithStatus(job, JobStatus.ABORTED);
      update(job);
    } else {
      logger.warn("Unknown job {}. Nothing to stop.", id);
    }
  }

  @Override
  public Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException {
    return jobHelper.createReadyJobs(rootId, setResources);
  }

  @Override
  public Job get(UUID id) {
    return jobRepository.get(id);
  }

  public void delete(UUID rootId, UUID jobId) {
    jobRepository.delete(rootId, Sets.newHashSet(jobId));
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
  public void handleJobsReady(Set<Job> jobs, UUID rootId, String producedByNode) {
    logger.debug("handleJobsReady(jobs={}, rootId={})", jobs.stream().map(Job::getName).collect(Collectors.toList()), rootId);
    try {
      engineStatusCallback.onJobsReady(jobs, rootId, producedByNode);
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    } finally {
      if (!jobs.isEmpty()) {
        jobRepository.delete(rootId, jobs.stream().filter(job -> !job.isRoot()).map(Job::getId).collect(Collectors.toSet()));
      }
    }
  }

  @Override
  public void handlePendingReadyJobs() {
    readyJobsByRootId().forEach((rootId, readyJobs) -> {
      groupByProducedBy(readyJobs).forEach((producedBy, ready) -> {
        handleJobsReady(ready.stream().map(JobEntity::getJob).collect(Collectors.toSet()), rootId, producedBy);
      });
    });
  }

  @Override
  public void handleJobFailed(final Job failedJob){
    logger.warn("Job {}, rootId: {} failed: {}", failedJob.getName(), failedJob.getRootId(), failedJob.getMessage());
    intermediaryFilesService.handleJobFailed(failedJob, jobRepository.get(failedJob.getRootId()));

    try {
      engineStatusCallback.onJobFailed(failedJob.getId(), failedJob.getRootId());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    } finally {
      jobRepository.deleteByRootIds(Sets.newHashSet(failedJob.getRootId()));
    }
  }

  @Override
  public void handleJobContainerReady(Job containerJob) {
    try {
      engineStatusCallback.onJobContainerReady(containerJob.getId(), containerJob.getRootId());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    }
  }

  @Override
  public void handleJobRootCompleted(Job job) {
    logger.info("Root job {} completed.", job.getId());
    if (deleteFilesUponExecution) {
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
      engineStatusCallback.onJobRootCompleted(job.getRootId());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    } finally {
      garbageCollectionService.forceGc(job.getRootId());
    }
  }

  @Override
  public void handleJobRootFailed(Job job){
    logger.warn("Root job {} failed.", job.getId());
    synchronized (stoppingRootIds) {
      if (deleteFilesUponExecution) {
        if (isLocalBackend) {
          try {
            Thread.sleep(FREE_RESOURCES_WAIT_TIME);
          } catch (InterruptedException e) {
          }
        }
      }

      job = Job.cloneWithStatus(job, JobStatus.FAILED);
      jobRepository.update(job);
      stoppingRootIds.remove(job.getId());
    }
    try {
      engineStatusCallback.onJobRootFailed(job.getRootId(), job.getMessage());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    } finally {
      garbageCollectionService.forceGc(job.getRootId());
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
      engineStatusCallback.onJobRootAborted(rootJob.getRootId());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed", e);
    } finally {
      garbageCollectionService.forceGc(rootJob.getRootId());
    }
  }

  @Override
  public void handleJobCompleted(Job job){
    logger.info("Job id: {}, name:{}, rootId: {} is completed.", job.getId(), job.getName(), job.getRootId());
    try{
      engineStatusCallback.onJobCompleted(job.getId(), job.getRootId());
    } catch (EngineStatusCallbackException e) {
      logger.error("Engine status callback failed",e);
    }
  }

  private Map<UUID, List<JobEntity>> readyJobsByRootId() {
    return jobRepository
            .getByStatus(Job.JobStatus.READY)
            .stream()
            .collect(groupingBy(jobEntity -> jobEntity.getJob().getRootId()));
  }

  private Map<String, List<JobEntity>> groupByProducedBy(List<JobEntity> jobEntities) {
    return jobEntities.stream().collect(groupingBy(JobEntity::getProducedByNode));
  }
}
