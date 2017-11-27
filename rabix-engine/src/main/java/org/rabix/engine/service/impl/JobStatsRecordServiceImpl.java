package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.engine.service.JobStatsRecordService;
import org.rabix.engine.store.lru.stats.JobStatsRecordCache;
import org.rabix.engine.store.model.JobStatsRecord;
import org.rabix.engine.store.repository.JobStatsRecordRepository;

import java.util.UUID;

public class JobStatsRecordServiceImpl implements JobStatsRecordService {

  private JobStatsRecordRepository jobStatsRecordRepository;

  private JobStatsRecordCache jobStatsRecordCache;

  @Inject
  public JobStatsRecordServiceImpl(JobStatsRecordRepository jobStatsRecordRepository,
                                   JobStatsRecordCache jobStatsRecordCache) {
    this.jobStatsRecordRepository = jobStatsRecordRepository;
    this.jobStatsRecordCache = jobStatsRecordCache;
  }

  public void create(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.insert(jobStatsRecord);
    jobStatsRecordCache.put(jobStatsRecord.getRootId(), jobStatsRecord);
  }

  public void update(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.update(jobStatsRecord);
    jobStatsRecordCache.put(jobStatsRecord.getRootId(), jobStatsRecord);
  }

  public JobStatsRecord find(UUID rootId) {
    JobStatsRecord jobStatsRecord = jobStatsRecordCache.get(rootId);
    if (jobStatsRecord == null) {
      jobStatsRecordRepository.get(rootId);
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
