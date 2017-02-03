package org.rabix.engine.repository;

import java.util.List;
import java.util.UUID;

import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CachableRepository;
import org.rabix.engine.model.JobRecord;

public abstract class JobRecordRepository implements CachableRepository {

  @Override
  public int insert(Cachable record) {
    return insert((JobRecord) record);
  }
  
  @Override
  public int update(Cachable record) {
    return update((JobRecord) record);
  }

  public abstract int insert(JobRecord jobRecord);
  
  public abstract int update(JobRecord jobRecord);
  
  public abstract List<JobRecord> get(UUID rootId);
  
  public abstract JobRecord getRoot(UUID rootId);
  
  public abstract JobRecord get(String jobName, UUID rootId);
  
  public abstract List<JobRecord> getByParent(UUID parentId, UUID rootId);
  
  public abstract List<JobRecord> getReady(UUID rootId);
  
}
