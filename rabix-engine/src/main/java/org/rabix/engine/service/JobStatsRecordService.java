package org.rabix.engine.service;

import org.rabix.engine.model.JobStatsRecord;

import java.util.UUID;

public interface JobStatsRecordService {

  void create(JobStatsRecord jobStatsRecord);
  
  void update(JobStatsRecord jobStatsRecord);

  JobStatsRecord find(UUID jobId);

  JobStatsRecord findOrCreate(UUID jobId);

  void delete(UUID jobId);
}
