package org.rabix.engine.service;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.storage.model.VariableRecord;

public interface VariableRecordService {

  void create(VariableRecord variableRecord);
  
  void update(VariableRecord variableRecord);

  VariableRecord find(String jobId, String portId, DAGLinkPort.LinkPortType type, UUID rootId);

  Object getValue(VariableRecord variableRecord);
  
  List<VariableRecord> find(String jobId, LinkPortType type, UUID rootId);
  
  List<VariableRecord> find(String jobId, String portId, UUID rootId);

  void addValue(VariableRecord variableRecord, Object value, Integer position, boolean wrap);
  
  Object linkMerge(VariableRecord variableRecord);

}
