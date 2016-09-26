package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.VariableRecord;

public class VariableRecordService {

  private ConcurrentMap<String, List<VariableRecord>> variableRecordsPerContext = new ConcurrentHashMap<String, List<VariableRecord>>();

  public void create(VariableRecord variableRecord) {
    getVariableRecords(variableRecord.getContextId()).add(variableRecord);
  }
  
  public void delete(String rootId) {
    variableRecordsPerContext.remove(rootId);
  }

  public void update(VariableRecord variableRecord) {
    for (VariableRecord vr : getVariableRecords(variableRecord.getContextId())) {
      if (vr.getJobId().equals(variableRecord.getJobId()) && vr.getPortId().equals(variableRecord.getPortId()) && vr.getType().equals(variableRecord.getType()) && vr.getContextId().equals(variableRecord.getContextId())) {
        vr.setValue(variableRecord.getValue());
        return;
      }
    }
  }
  
  public List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }
  
  public List<VariableRecord> find(String jobId, String portId, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        return vr;
      }
    }
    return null;
  }

  public List<VariableRecord> findByJobId(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public List<VariableRecord> find(String contextId) {
    return getVariableRecords(contextId);
  }
  
  public List<VariableRecord> getVariableRecords(String contextId) {
    List<VariableRecord> variableList = variableRecordsPerContext.get(contextId);
    if (variableList == null) {
      variableList = new ArrayList<>();
      variableRecordsPerContext.put(contextId, variableList);
    }
    return variableList;
  }
  
}
