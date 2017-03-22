package org.rabix.engine.service.impl;

import java.util.UUID;

import org.rabix.engine.model.JobStatsRecord;
import org.rabix.engine.repository.JobStatsRecordRepository;
import org.rabix.engine.service.JobStatsRecordService;

import com.google.inject.Inject;

public class JobStatsRecordServiceImpl implements JobStatsRecordService {

  private JobStatsRecordRepository jobStatsRecordRepository;

  @Inject
  public JobStatsRecordServiceImpl(JobStatsRecordRepository jobStatsRecordRepository) {
    this.jobStatsRecordRepository = jobStatsRecordRepository;
  }


  public void create(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.insert(jobStatsRecord);
  }
  
  public void update(JobStatsRecord jobStatsRecord) {
    jobStatsRecordRepository.update(jobStatsRecord);
  }
  
  public JobStatsRecord find(UUID rootId) {
    return jobStatsRecordRepository.get(rootId);
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
  }

}
