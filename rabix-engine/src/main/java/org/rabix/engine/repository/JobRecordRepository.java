package org.rabix.engine.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CachableRepository;
import org.rabix.engine.model.JobRecord;

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
  
  public abstract List<JobRecord> get(String rootId);
  
  public abstract JobRecord getRoot(String rootId);
  
  public abstract JobRecord get(String id, String rootId);
  
  public abstract List<JobRecord> getByParent(String parentId, String rootId);
  
  public abstract List<JobRecord> getReady(String rootId);
  
}
