package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.metrics.MetricsHelper;
import org.rabix.engine.service.GarbageCollectionService;
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

  private final TransactionHelper transactionHelper;
  private final MetricsHelper metricsHelper;
  private final ExecutorService executorService;
  private final Set<UUID> pendingGCs;

  private final boolean enabled;
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

  private void doGc(UUID rootId) {
    JobRecord root = jobRecordRepository.getRoot(rootId);
    if (inTerminalState(root)) {
      collect(root);
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
    if (jobRecord == null) {
      return;
    }

    logger.info("Collecting garbage of {} with id {}", jobRecord.getId(), jobRecord.getExternalId());

    UUID rootId = jobRecord.getRootId();
    List<JobRecord> garbage = new ArrayList<>();
    garbage.add(jobRecord);

    if (jobRecord.isRoot()) {
      dagRepository.delete(rootId);
      linkRecordRepository.deleteByRootId(rootId);
      jobStatsRecordRepository.delete(rootId);
      eventRepository.deleteByRootId(rootId);
      variableRecordRepository.deleteByRootId(rootId);
      linkRecordRepository.deleteByRootId(rootId);

      List<JobRecord> all = jobRecordRepository.get(rootId);
      garbage.addAll(all);
    } else if(jobRecord.isScatterWrapper()) {
      garbage.addAll(jobRecordRepository.getByParent(jobRecord.getExternalId(), rootId));
    }

    flush(rootId, garbage);
  }

  private void flush(UUID rootId, List<JobRecord> garbage) {
    garbage.forEach(record -> {
      jobRecordRepository.delete(record.getExternalId(), record.getRootId());
      jobRepository.delete(record.getRootId(), new HashSet<>(Collections.singletonList(record.getExternalId())));
    });

    Set<UUID> groupIds = garbage.stream().map(JobRecord::getExternalId).collect(Collectors.toSet());
    if (!groupIds.isEmpty()) {
      eventRepository.deleteByGroupIds(rootId, groupIds);
    }
  }

  private boolean isGarbage(JobRecord jobRecord) {
    List<LinkRecord> outputLinks = linkRecordRepository.getBySource(jobRecord.getId(), jobRecord.getRootId());
    List<JobRecord> outputJobRecords = outputLinks
            .stream()
            .map(link -> jobRecordRepository.get(link.getDestinationJobId(), link.getRootId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return outputJobRecords.isEmpty() || outputJobRecords.stream().allMatch(this::inTerminalState);
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
