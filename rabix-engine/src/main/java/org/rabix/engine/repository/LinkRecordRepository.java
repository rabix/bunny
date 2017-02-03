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
  
  public abstract List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, String rootId);
  
  public abstract List<LinkRecord> getBySourceJobId(String sourceJobId, String rootId);
  
  public abstract List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, String rootId);
  
  public abstract List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, UUID rootId);
  
}
