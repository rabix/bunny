package org.rabix.engine.store.repository;

import org.rabix.engine.store.model.JobStatsRecord;

import java.util.UUID;

public interface JobStatsRecordRepository {

  int insert(JobStatsRecord jobStatsRecord);
  
  int update(JobStatsRecord jobStatsRecord);

  JobStatsRecord get(UUID rootId);

  int delete(UUID rootId);

}
