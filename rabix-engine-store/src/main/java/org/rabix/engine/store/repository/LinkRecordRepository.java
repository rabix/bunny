package org.rabix.engine.store.repository;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.store.model.LinkRecord;

public abstract class LinkRecordRepository {

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
