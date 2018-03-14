package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.VariableRecord;
import org.rabix.engine.store.repository.VariableRecordRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryVariableRecordRepository extends VariableRecordRepository {

  private ConcurrentMap<UUID, Collection<VariableRecord>> variableRecordsPerContext;
  private ConcurrentMap<UUID, ConcurrentMap<String, Collection<VariableRecord>>> variableRecordsPerContextAndId;

  @Inject
    public InMemoryVariableRecordRepository() {
    variableRecordsPerContext = new ConcurrentHashMap<>();
    variableRecordsPerContextAndId = new ConcurrentHashMap<>();
  }

  public int insert(VariableRecord variableRecord) {
    getVariableRecords(variableRecord.getRootId()).add(variableRecord);
    getVariableRecordsWithId(variableRecord.getRootId(), variableRecord.getJobId()).add(variableRecord);
    return 1;
  }

  public void delete(UUID rootId) {
    variableRecordsPerContext.remove(rootId);
  }

  public int update(VariableRecord variableRecord) {
    for (VariableRecord vr : getVariableRecordsWithId(variableRecord.getRootId(), variableRecord.getJobId())) {
      if (vr.getPortId().equals(variableRecord.getPortId()) && vr.getType().equals(variableRecord.getType())) {
        vr.setValue(variableRecord.getValue());
        return 1;
      }
    }
    return 0;
  }

  public List<VariableRecord> getByType(String jobId, LinkPortType type, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecordsWithId(contextId, jobId)) {
      if (vr.getType().equals(type)) {
        result.add(vr);
      }
    }
    return result;
  }

  public VariableRecord get(String jobId, String portId, LinkPortType type, UUID contextId) {
    for (VariableRecord vr : getVariableRecordsWithId(contextId, jobId)) {
      if (vr.getPortId().equals(portId) && vr.getType().equals(type)) {
        return vr;
      }
    }
    return null;
  }


  public List<VariableRecord> getByPort(String jobId, String portId, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecordsWithId(contextId, jobId)) {
      if (vr.getPortId().equals(portId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public List<VariableRecord> findByJobId(String jobId, LinkPortType type, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecordsWithId(contextId, jobId)) {
      if (vr.getType().equals(type)) {
        result.add(vr);
      }
    }
    return result;
  }

  public List<VariableRecord> find(UUID contextId) {
    return new ArrayList<>(getVariableRecords(contextId));
  }

  public Collection<VariableRecord> getVariableRecords(UUID contextId) {
    return variableRecordsPerContext.computeIfAbsent(contextId, k -> new ArrayList<>());
  }

  public Collection<VariableRecord> getVariableRecordsWithId(UUID contextId, String jobId) {
    ConcurrentMap<String, Collection<VariableRecord>> map = variableRecordsPerContextAndId.computeIfAbsent(contextId, k -> new ConcurrentHashMap<>());
    return map.computeIfAbsent(jobId, k-> new ArrayList<>());
  }

  @Override
  public void insertBatch(Iterator<VariableRecord> records) {
    while(records.hasNext()) {
      insert(records.next());
    }
  }

  @Override
  public void updateBatch(Iterator<VariableRecord> records) {
    while(records.hasNext()) {
      update(records.next());
    }
  }

  @Override
  public void delete(String id, UUID rootId) {
    getVariableRecords(rootId).removeIf(variableRecord -> variableRecord.getJobId().equals(id));
  }

  @Override
  public void deleteByRootId(UUID rootId) {
    variableRecordsPerContext.remove(rootId);
  }
}
