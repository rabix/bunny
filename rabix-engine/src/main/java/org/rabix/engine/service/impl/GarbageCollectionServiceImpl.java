package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.engine.service.GarbageCollectionService;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GarbageCollectionServiceImpl implements GarbageCollectionService {

  private final static Logger logger = LoggerFactory.getLogger(GarbageCollectionServiceImpl.class);

  private final TransactionHelper transactionService;
  private final JobRepository jobRepository;
  private final JobRecordRepository jobRecordRepository;
  private final EventRepository eventRepository;
  private final VariableRecordRepository variableRecordRepository;
  private final LinkRecordRepository linkRecordRepository;
  private final DAGRepository dagRepository;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "GarbageCollectionService"));

  @Inject
  public GarbageCollectionServiceImpl(JobRepository jobRepository,
                                      JobRecordRepository jobRecordRepository,
                                      EventRepository eventRepository,
                                      VariableRecordRepository variableRecordRepository,
                                      LinkRecordRepository linkRecordRepository,
                                      DAGRepository dagRepository,
                                      TransactionHelper transactionService) {
    this.jobRepository = jobRepository;
    this.jobRecordRepository = jobRecordRepository;
    this.transactionService = transactionService;
    this.eventRepository = eventRepository;
    this.variableRecordRepository = variableRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.dagRepository = dagRepository;
  }

  public void gc(UUID rootId) {
    logger.info("gc(rootId={})", rootId);
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
    List<JobRecord> jobRecords = jobRecordRepository.get(rootId, terminalStates());
    jobRecords.stream().filter(this::isGarbage).forEach(this::collect);
  }

  private void collect(JobRecord jobRecord) {
    logger.info("Collecting garbage of {} with id {}", jobRecord.getId(), jobRecord.getExternalId());

    UUID rootId = jobRecord.getRootId();
    if (jobRecord.isRoot()) {
      dagRepository.delete(rootId);
      linkRecordRepository.deleteByRootId(rootId);
    }

    variableRecordRepository.delete(jobRecord.getId(), rootId);
    eventRepository.deleteGroup(jobRecord.getExternalId());
    jobRecordRepository.delete(jobRecord.getExternalId(), rootId);
    jobRepository.delete(rootId, new HashSet<>(Collections.singletonList(jobRecord.getExternalId())));
  }

  private boolean isGarbage(JobRecord jobRecord) {
    return jobRecord.getOutputCounters().stream().allMatch(portCounter -> portCounter.counter <= 0);
  }

  private Set<JobRecord.JobState> terminalStates() {
    return new HashSet<>(
            Arrays.asList(JobRecord.JobState.COMPLETED, JobRecord.JobState.ABORTED, JobRecord.JobState.FAILED));
  }
}
