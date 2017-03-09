package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobStatsRecord;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.repository.JobStatsRecordRepository;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobStatsRecordService;

import java.util.UUID;

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
  
  public JobStatsRecord find(UUID jobId) {
    return jobStatsRecordRepository.get(jobId);
  }

  @Override public JobStatsRecord findOrCreate(UUID jobId) {
    JobStatsRecord jobStatsRecord = find(jobId);
    if (jobStatsRecord == null) {
      jobStatsRecord = new JobStatsRecord(jobId, 0, 0);
      create(jobStatsRecord);
    }
    return jobStatsRecord;
  }

  public void delete(UUID jobId) {
    jobStatsRecordRepository.delete(jobId);
  }

}
