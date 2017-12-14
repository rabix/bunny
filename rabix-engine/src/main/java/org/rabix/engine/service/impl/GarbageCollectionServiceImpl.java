package org.rabix.engine.service.impl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.metrics.MetricsHelper;
import org.rabix.engine.service.GarbageCollectionService;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.model.ContextRecord.ContextStatus;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class GarbageCollectionServiceImpl implements GarbageCollectionService {

  private final static Logger logger = LoggerFactory.getLogger(GarbageCollectionServiceImpl.class);

  private final JobRepository jobRepository;
  private final JobRecordRepository jobRecordRepository;
  private final JobStatsRecordRepository jobStatsRecordRepository;
  private final EventRepository eventRepository;
  private final VariableRecordRepository variableRecordRepository;
  private final LinkRecordRepository linkRecordRepository;
  private final DAGRepository dagRepository;
  private final ContextRecordRepository contextRecordRepository;
  private final IntermediaryFilesRepository intermediaryFilesRepository;

  private final TransactionHelper transactionHelper;
  private final MetricsHelper metricsHelper;
  private final ExecutorService executorService;
  private final Set<UUID> pendingGCs;

  private volatile boolean enabled;
  private final int numberOfGcThreads;

  private final Set<JobRecord.JobState> terminalStates = new HashSet<>(Arrays.asList(
          JobRecord.JobState.COMPLETED,
          JobRecord.JobState.ABORTED,
          JobRecord.JobState.FAILED));

  @Inject
  public GarbageCollectionServiceImpl(JobRepository jobRepository,
                                      JobRecordRepository jobRecordRepository,
                                      JobStatsRecordRepository jobStatsRecordRepository,
                                      EventRepository eventRepository,
                                      VariableRecordRepository variableRecordRepository,
                                      LinkRecordRepository linkRecordRepository,
                                      DAGRepository dagRepository,
                                      ContextRecordRepository contextRecordRepository,
                                      IntermediaryFilesRepository intermediaryFilesRepository,
                                      TransactionHelper transactionHelper,
                                      MetricsHelper metricsHelper,
                                      Configuration configuration) {
    this.jobRepository = jobRepository;
    this.jobRecordRepository = jobRecordRepository;
    this.jobStatsRecordRepository = jobStatsRecordRepository;
    this.eventRepository = eventRepository;
    this.variableRecordRepository = variableRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.dagRepository = dagRepository;
    this.contextRecordRepository = contextRecordRepository;
    this.intermediaryFilesRepository = intermediaryFilesRepository;

    this.transactionHelper = transactionHelper;
    this.metricsHelper = metricsHelper;

    this.pendingGCs = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.enabled = configuration.getBoolean("gc.enabled", true);
    this.numberOfGcThreads = configuration.getInt("gc.threads.number", Runtime.getRuntime().availableProcessors() + 1);

    this.executorService = buildExecutorService();
  }

  public void gc(UUID rootId) {
    if (!enabled) {
      return;
    }

    logger.info("gc(rootId={})", rootId);

    if (pendingGCs.contains(rootId)) {
      logger.info("gc already scheduled for {}", rootId);
      return;
    }

    pendingGCs.add(rootId);
    Runnable gcRun = () -> {
      try {
        metricsHelper.time(() -> inTransaction(() -> doGc(rootId)), "gc");
      } finally {
        pendingGCs.remove(rootId);
      }
    };
    executorService.submit(gcRun);
  }

  @Override
  public void forceGc(UUID rootId) {
    if (!enabled) {
      return;
    }

    flushAll(rootId);
  }

  @Override
  public void enable() {
    this.enabled = true;
  }

  @Override
  public void disable() {
    this.enabled = false;
  }

  private void doGc(UUID rootId) {
    JobRecord root = jobRecordRepository.getRoot(rootId);

    if (root == null || isRootCompleted(root)) {
      flushAll(rootId);
    } else {
      List<JobRecord> jobRecords = jobRecordRepository
              .get(rootId, terminalStates)
              .stream()
              .filter(jobRecord -> !jobRecord.isScattered())
              .collect(Collectors.toList());
      jobRecords.stream().filter(this::isGarbage).forEach(this::collect);
    }
  }

  private void collect(JobRecord jobRecord) {
    logger.info("Collecting garbage of {} with id {}", jobRecord.getId(), jobRecord.getExternalId());

    UUID rootId = jobRecord.getRootId();
    if (jobRecord.isRoot()) {
      flushAll(rootId);
    } else {
      List<JobRecord> garbage = new ArrayList<>();
      garbage.add(jobRecord);

      if(jobRecord.isScatterWrapper() || jobRecord.isContainer()) {
        garbage.addAll(jobRecordRepository.getByParent(jobRecord.getExternalId(), rootId));
      }

      flush(rootId, garbage);
    }
  }

  private void flush(UUID rootId, List<JobRecord> garbage) {
    garbage.forEach(record -> {
      jobRecordRepository.delete(record.getExternalId(), record.getRootId());
      jobRepository.delete(record.getRootId(), new HashSet<>(Collections.singletonList(record.getExternalId())));
      linkRecordRepository.delete(record.getId(), rootId);
      variableRecordRepository.delete(record.getId(), rootId);
    });

    Set<UUID> groupIds = garbage.stream().map(JobRecord::getExternalId).collect(Collectors.toSet());
    if (!groupIds.isEmpty()) {
      eventRepository.deleteByGroupIds(rootId, groupIds);
    }
  }

  private void flushAll(UUID rootId) {
    logger.info("flushAll(rootId={})", rootId);

    dagRepository.delete(rootId);
    jobStatsRecordRepository.delete(rootId);
    eventRepository.deleteByRootId(rootId);
    variableRecordRepository.deleteByRootId(rootId);
    linkRecordRepository.deleteByRootId(rootId);
    contextRecordRepository.delete(rootId);
    intermediaryFilesRepository.delete(rootId);
    jobRepository.deleteByRootIds(Sets.newHashSet(rootId));

    List<JobRecord> all = jobRecordRepository.get(rootId);
    flush(rootId, all);
  }

  private boolean isGarbage(JobRecord jobRecord) {
    List<LinkRecord> outputLinks = linkRecordRepository.getBySource(jobRecord.getId(), jobRecord.getRootId());
    List<JobRecord> outputJobRecords = outputLinks
            .stream()
            .map(link -> jobRecordRepository.get(link.getDestinationJobId(), link.getRootId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return outputJobRecords.isEmpty() || outputJobRecords.stream().allMatch(outputJobRecord -> {
      if (!outputJobRecord.isReady() || !inTerminalState(outputJobRecord)) {
        return false;
      }
      return !outputJobRecord.isContainer() || isGarbage(outputJobRecord);
    });
  }

  private boolean inTerminalState(JobRecord jobRecord) {
    return jobRecord == null || terminalStates.contains(jobRecord.getState());
  }

  private void inTransaction(Runnable runnable) {
    try {
      transactionHelper.doInTransaction(() -> {
        runnable.run();
        return null;
      });
    } catch (Exception e) {
      logger.warn("Exception in gc transaction.", e);
    }
  }

  private boolean isRootCompleted(JobRecord root) {
    if (inTerminalState(root)) {
      return true;
    }
    ContextRecord contextRecord = contextRecordRepository.get(root.getRootId());
    return contextRecord == null || contextRecord.getStatus() != ContextStatus.RUNNING;
  }

  private ExecutorService buildExecutorService() {
    return Executors.newFixedThreadPool(numberOfGcThreads, new ThreadFactory() {
      int count;
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "GarbageCollectionThread" + ++count);
        thread.setDaemon(true);
        return thread;
      }
    });
  }
}
