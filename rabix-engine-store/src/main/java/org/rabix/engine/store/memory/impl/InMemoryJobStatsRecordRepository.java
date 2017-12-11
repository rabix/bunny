package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.JobStatsRecord;
import org.rabix.engine.store.repository.JobStatsRecordRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJobStatsRecordRepository implements JobStatsRecordRepository {

  Map<UUID, JobStatsRecord> jobStatsRecordRepository;

  @Inject
  public InMemoryJobStatsRecordRepository() {
    this.jobStatsRecordRepository = new ConcurrentHashMap<>();
  }

  @Override
  public int insert(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.put(jobStatsRecord.getRootId(), jobStatsRecord);
    return 1;
  }

  @Override
  public int update(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.put(jobStatsRecord.getRootId(), jobStatsRecord);
    return 1;
  }

  @Override
  public JobStatsRecord get(UUID id) {
    return jobStatsRecordRepository.get(id);
  }

  @Override
  public int delete(UUID id) {
    jobStatsRecordRepository.remove(id);
    return 1;
  }

}
