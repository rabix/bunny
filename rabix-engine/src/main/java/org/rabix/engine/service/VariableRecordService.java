package org.rabix.engine.service;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.storage.model.VariableRecord;
import org.rabix.storage.model.scatter.VariableFinder;

public interface VariableRecordService extends VariableFinder{

  void create(VariableRecord variableRecord);
  
  void update(VariableRecord variableRecord);
  
  List<VariableRecord> find(String jobId, LinkPortType type, UUID rootId);
  
  List<VariableRecord> find(String jobId, String portId, UUID rootId);

  void addValue(VariableRecord variableRecord, Object value, Integer position, boolean wrap);
  
  Object linkMerge(VariableRecord variableRecord);

}
