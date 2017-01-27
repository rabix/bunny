package org.rabix.engine.repository;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.VariableRecord;

public interface VariableRecordRepository {

  int insert(VariableRecord jobRecord);
  
  int update(VariableRecord jobRecord);
  
  VariableRecord get(String jobId, String portId, LinkPortType type, String rootId);
 
  List<VariableRecord> getByType(String jobId, LinkPortType type, String rootId);
  
  List<VariableRecord> getByPort(String jobId, String portId, String rootId);
 
}
