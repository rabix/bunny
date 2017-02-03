package org.rabix.engine.repository;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CachableRepository;
import org.rabix.engine.model.LinkRecord;

public abstract class LinkRecordRepository implements CachableRepository {

  @Override
  public int insert(Cachable record) {
    return insert((LinkRecord) record);
  }
  
  @Override
  public int update(Cachable record) {
    return update((LinkRecord) record);
  }
  
  public abstract int insert(LinkRecord linkRecord);
  
  public abstract int update(LinkRecord linkRecord);
  
  public abstract List<LinkRecord> getBySourcePort(String sourceJobName, String sourceJobPortId, UUID rootId);
  
  public abstract List<LinkRecord> getBySourceJob(String sourceJobName, UUID rootId);
  
  public abstract List<LinkRecord> getBySourceJobAndSourceType(String sourceJobName, LinkPortType sourceType, UUID rootId);
  
  public abstract List<LinkRecord> getBySourcePortAndDestinationType(String sourceJobName, String sourceJobPortId, LinkPortType destinationType, UUID rootId);
  
}
