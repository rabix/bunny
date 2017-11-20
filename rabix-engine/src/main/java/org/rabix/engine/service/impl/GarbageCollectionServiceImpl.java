package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.GarbageCollectionService;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GarbageCollectionServiceImpl implements GarbageCollectionService {

  private final static Logger logger = LoggerFactory.getLogger(GarbageCollectionServiceImpl.class);

  private final TransactionHelper transactionService;
  private final JobRepository jobRepository;
  private final JobRecordRepository jobRecordRepository;
  private final JobStatsRecordRepository jobStatsRecordRepository;
  private final EventRepository eventRepository;
  private final VariableRecordRepository variableRecordRepository;
  private final LinkRecordRepository linkRecordRepository;
  private final DAGRepository dagRepository;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "GarbageCollectionService"));

  private final boolean enabled;

  @Inject
  public GarbageCollectionServiceImpl(JobRepository jobRepository,
                                      JobRecordRepository jobRecordRepository,
                                      JobStatsRecordRepository jobStatsRecordRepository,
                                      EventRepository eventRepository,
                                      VariableRecordRepository variableRecordRepository,
                                      LinkRecordRepository linkRecordRepository,
                                      DAGRepository dagRepository,
                                      TransactionHelper transactionService,
                                      Configuration configuration) {
    this.jobRepository = jobRepository;
    this.jobRecordRepository = jobRecordRepository;
    this.jobStatsRecordRepository = jobStatsRecordRepository;
    this.transactionService = transactionService;
    this.eventRepository = eventRepository;
    this.variableRecordRepository = variableRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.dagRepository = dagRepository;
    this.enabled = configuration.getBoolean("gc.enabled", true);
  }

  public void gc(UUID rootId) {
    if (!enabled) return;

    executorService.submit(() -> {
      try {
        transactionService.doInTransaction((TransactionHelper.TransactionCallback<Void>) () -> {
          doGc(rootId);
          return null;
        });
      } catch (Exception e) {
        logger.warn("Could not perform garbage collection.", e);
      }
    });
  }

  private void doGc(UUID rootId) {
    List<JobRecord> jobRecords = jobRecordRepository
            .get(rootId, terminalStates())
            .stream()
            .filter(jobRecord -> !jobRecord.isScattered())
            .collect(Collectors.toList());
    jobRecords.stream().filter(this::isGarbage).forEach(this::collect);
  }

  private void collect(JobRecord jobRecord) {
    logger.info("Collecting garbage of {} with id {}", jobRecord.getId(), jobRecord.getExternalId());

    UUID rootId = jobRecord.getRootId();
    if (jobRecord.isRoot()) {
      dagRepository.delete(rootId);
      linkRecordRepository.deleteByRootId(rootId);
      jobStatsRecordRepository.delete(rootId);
      eventRepository.deleteByRootId(rootId);

      List<JobRecord> all = jobRecordRepository.get(rootId);
      flush(all);
    }

    List<JobRecord> garbage = new ArrayList<>();
    garbage.add(jobRecord);

    if (jobRecord.isScatterWrapper()) {
      List<JobRecord> scattered = jobRecordRepository.getByParent(jobRecord.getExternalId(), rootId);
      garbage.addAll(scattered);
    }

    flush(garbage);
  }

  private void flush(List<JobRecord> garbage) {
    garbage.forEach(record -> {
      variableRecordRepository.delete(record.getId(), record.getRootId());
      eventRepository.deleteGroup(record.getExternalId());
      jobRecordRepository.delete(record.getExternalId(), record.getRootId());
      jobRepository.delete(record.getRootId(), new HashSet<>(Collections.singletonList(record.getExternalId())));
    });
  }

  private boolean isGarbage(JobRecord jobRecord) {
    List<LinkRecord> outputLink = linkRecordRepository.getBySource(jobRecord.getId(), jobRecord.getRootId());
    List<JobRecord> outputJobRecords = outputLink
            .stream()
            .map(link -> jobRecordRepository.get(link.getDestinationJobId(), link.getRootId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return outputJobRecords.isEmpty() || outputJobRecords.stream().allMatch(outputRecord -> {
      if (outputRecord.isScatterWrapper()) {
        return isGarbage(outputRecord);
      }
      return outputRecord.isCompleted();
    });
  }

  private Set<JobRecord.JobState> terminalStates() {
    return new HashSet<>(
            Arrays.asList(JobRecord.JobState.COMPLETED, JobRecord.JobState.ABORTED, JobRecord.JobState.FAILED));
  }
}
