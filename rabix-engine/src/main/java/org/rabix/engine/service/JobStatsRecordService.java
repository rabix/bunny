package org.rabix.engine.service;

import org.rabix.engine.store.model.JobStatsRecord;

import java.util.UUID;

public interface JobStatsRecordService {

  void create(JobStatsRecord jobStatsRecord);
  
  void update(JobStatsRecord jobStatsRecord);

  JobStatsRecord find(UUID rootId);

  JobStatsRecord findOrCreate(UUID rootId);

  void delete(UUID rootId);
}
