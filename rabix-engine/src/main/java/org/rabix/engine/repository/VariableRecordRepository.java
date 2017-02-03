package org.rabix.engine.repository;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CachableRepository;
import org.rabix.engine.model.VariableRecord;

public abstract class VariableRecordRepository implements CachableRepository  {

  @Override
  public int insert(Cachable record) {
    return insert((VariableRecord) record);
  }
  
  @Override
  public int update(Cachable record) {
    return update((VariableRecord) record);
  }
  
  public abstract int insert(VariableRecord jobRecord);
  
  public abstract int update(VariableRecord jobRecord);
  
  public abstract VariableRecord get(String jobName, String portId, LinkPortType type, UUID rootId);
 
  public abstract List<VariableRecord> getByType(String jobName, LinkPortType type, UUID rootId);
  
  public abstract List<VariableRecord> getByPort(String jobName, String portId, UUID rootId);
 
}
