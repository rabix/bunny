package org.rabix.engine.store.repository;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.LinkRecord;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class LinkRecordRepository {

  public abstract void insertBatch(Iterator<LinkRecord> records);

  public abstract void updateBatch(Iterator<LinkRecord> records);

  public abstract void deleteByDestinationIdAndType(String destinationId, LinkPortType linkPortType, UUID rootId);

  public abstract void delete(String jobId, UUID rootId);

  public abstract int insert(LinkRecord linkRecord);

  public abstract int update(LinkRecord linkRecord);

  public abstract void deleteByRootId(UUID rootId);

  public abstract List<LinkRecord> getBySource(String sourceJobId, UUID rootId);

  public abstract List<LinkRecord> getByDestination(String destinationJobId, UUID rootId);

  public abstract int getBySourceCount(String sourceJobId, String sourceJobPortId, UUID rootId);

  public abstract List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, UUID rootId);

  public abstract List<LinkRecord> getBySourceJobId(String sourceJobId, UUID rootId);

  public abstract List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, UUID rootId);

  public abstract List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, UUID rootId);

  public abstract List<LinkRecord> getBySourceAndSourceType(String jobId, String portId, LinkPortType varType, UUID rootId);

}
