package org.rabix.engine.store.repository;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.VariableRecord;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class VariableRecordRepository {

  public abstract void insertBatch(Iterator<VariableRecord> records);

  public abstract void updateBatch(Iterator<VariableRecord> records);

  public abstract int insert(VariableRecord jobRecord);

  public abstract int update(VariableRecord jobRecord);

  public abstract void delete(String id, UUID rootId);

  public abstract void deleteByRootId(UUID rootId);

  public abstract VariableRecord get(String jobId, String portId, LinkPortType type, UUID rootId);

  public abstract List<VariableRecord> getByType(String jobId, LinkPortType type, UUID rootId);

  public abstract List<VariableRecord> getByPort(String jobId, String portId, UUID rootId);

}
