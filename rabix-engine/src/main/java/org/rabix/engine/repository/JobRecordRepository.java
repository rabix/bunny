package org.rabix.engine.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CachableRepository;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.service.JobRecordService.JobState;

public abstract class JobRecordRepository implements CachableRepository {

  @Override
  public int insertCachable(Cachable record) {
    return insert((JobRecord) record);
  }
  
  @Override
  public int updateCachable(Cachable record) {
    return update((JobRecord) record);
  }
  
  @Override
  public void insertCachables(List<Cachable> cachables) {
    List<JobRecord> records = new ArrayList<>();
    for (Cachable cachable : cachables) {
      records.add((JobRecord) cachable);
    }
    insertBatch(records.iterator());
  }
  
  @Override
  public void updateCachables(List<Cachable> cachables) {
    List<JobRecord> records = new ArrayList<>();
    for (Cachable cachable : cachables) {
      records.add((JobRecord) cachable);
    }
    updateBatch(records.iterator());
  }

  public abstract int insert(JobRecord jobRecord);
  
  public abstract int update(JobRecord jobRecord);
  
  public abstract void insertBatch(Iterator<JobRecord> records);
  
  public abstract void updateBatch(Iterator<JobRecord> records);
  
  public abstract int deleteByStatus(JobState state);
  
  public abstract void delete(Set<JobIdRootIdPair> externalIDs);
  
  public abstract List<JobRecord> get(UUID rootId);
  
  public abstract JobRecord getRoot(UUID rootId);
  
  public abstract JobRecord get(String id, UUID rootId);
  
  public abstract List<JobRecord> getByParent(UUID parentId, UUID rootId);
  
  public abstract List<JobRecord> getReady(UUID rootId);
  
}
