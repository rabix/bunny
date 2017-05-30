package org.rabix.engine.store.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.cache.Cachable;
import org.rabix.engine.store.cache.CachableRepository;
import org.rabix.engine.store.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.store.model.LinkRecord;

public abstract class LinkRecordRepository implements CachableRepository {

  @Override
  public int insertCachable(Cachable record) {
    return insert((LinkRecord) record);
  }
  
  @Override
  public int updateCachable(Cachable record) {
    return update((LinkRecord) record);
  }
  
  @Override
  public void insertCachables(List<Cachable> cachables) {
    List<LinkRecord> records = new ArrayList<>();
    for (Cachable cachable : cachables) {
      records.add((LinkRecord) cachable);
    }
    insertBatch(records.iterator());
  }
  
  @Override
  public void updateCachables(List<Cachable> cachables) {
    List<LinkRecord> records = new ArrayList<>();
    for (Cachable cachable : cachables) {
      records.add((LinkRecord) cachable);
    }
    updateBatch(records.iterator());
  }

  public abstract void insertBatch(Iterator<LinkRecord> records);
  
  public abstract void updateBatch(Iterator<LinkRecord> records);
  
  public abstract void delete(Set<JobIdRootIdPair> pairs);
  
  public abstract int insert(LinkRecord linkRecord);
  
  public abstract int update(LinkRecord linkRecord);
  
  public abstract List<LinkRecord> getBySource(String sourceJobId, UUID rootId);
  
  public abstract int getBySourceCount(String sourceJobId, String sourceJobPortId, UUID rootId);
  
  public abstract List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, UUID rootId);
  
  public abstract List<LinkRecord> getBySourceJobId(String sourceJobId, UUID rootId);
  
  public abstract List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, UUID rootId);
  
  public abstract List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, UUID rootId);

  public abstract List<LinkRecord> getBySourceAndSourceType(String jobId, String portId, LinkPortType varType, UUID rootId);

  
}
