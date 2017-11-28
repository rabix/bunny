package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.JobStatsRecordService;
import org.rabix.engine.store.lru.stats.JobStatsRecordCache;
import org.rabix.engine.store.model.JobStatsRecord;
import org.rabix.engine.store.repository.JobStatsRecordRepository;

import java.util.UUID;

public class JobStatsRecordServiceImpl implements JobStatsRecordService {

  private JobStatsRecordRepository jobStatsRecordRepository;

  private JobStatsRecordCache jobStatsRecordCache;

  private EventProcessor eventProcessor;

  @Inject
  public JobStatsRecordServiceImpl(JobStatsRecordRepository jobStatsRecordRepository,
                                   JobStatsRecordCache jobStatsRecordCache,
                                   EventProcessor eventProcessor) {
    this.jobStatsRecordRepository = jobStatsRecordRepository;
    this.jobStatsRecordCache = jobStatsRecordCache;
    this.eventProcessor = eventProcessor;
  }

  public void create(JobStatsRecord jobStatsRecord) {
    if (eventProcessor.isReplayMode()) {
      return;
    }

    int inserted = jobStatsRecordRepository.insert(jobStatsRecord);
    if (inserted > 0) {
      jobStatsRecordCache.put(jobStatsRecord.getRootId(), jobStatsRecord);
    }
  }

  public void update(JobStatsRecord jobStatsRecord) {
    if (eventProcessor.isReplayMode()) {
      return;
    }

    int updated = jobStatsRecordRepository.update(jobStatsRecord);
    if (updated > 0) {
      jobStatsRecordCache.put(jobStatsRecord.getRootId(), jobStatsRecord);
    }
  }

  public JobStatsRecord find(UUID rootId) {
    JobStatsRecord jobStatsRecord = jobStatsRecordCache.get(rootId);
    if (jobStatsRecord == null) {
      jobStatsRecord = jobStatsRecordRepository.get(rootId);
    }

    if (jobStatsRecord != null) {
      jobStatsRecordCache.put(rootId, jobStatsRecord);
    }

    return jobStatsRecord;
  }

  @Override public JobStatsRecord findOrCreate(UUID rootId) {
    JobStatsRecord jobStatsRecord = find(rootId);
    if (jobStatsRecord == null) {
      jobStatsRecord = new JobStatsRecord(rootId, 0, 0, 0);
      create(jobStatsRecord);
    }
    return jobStatsRecord;
  }

  public void delete(UUID rootId) {
    jobStatsRecordRepository.delete(rootId);
    jobStatsRecordCache.remove(rootId);
  }

}
