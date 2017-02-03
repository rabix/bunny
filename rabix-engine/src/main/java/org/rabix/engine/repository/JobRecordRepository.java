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
  
  public abstract List<JobRecord> get(String rootId);
  
  public abstract JobRecord getRoot(String rootId);
  
  public abstract JobRecord get(String id, UUID rootId);
  
  public abstract List<JobRecord> getByParent(String parentId, String rootId);
  
  public abstract List<JobRecord> getReady(String rootId);
  
}
