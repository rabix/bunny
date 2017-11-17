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

  @Inject
  public InMemoryVariableRecordRepository() {
    variableRecordsPerContext = new ConcurrentHashMap<>();
  }

  public int insert(VariableRecord variableRecord) {
    getVariableRecords(variableRecord.getRootId()).add(variableRecord);
    return 1;
  }

  public void delete(UUID rootId) {
    variableRecordsPerContext.remove(rootId);
  }

  public int update(VariableRecord variableRecord) {
    for (VariableRecord vr : getVariableRecords(variableRecord.getRootId())) {
      if (vr.getJobId().equals(variableRecord.getJobId()) && vr.getPortId().equals(variableRecord.getPortId()) && vr.getType().equals(variableRecord.getType()) && vr.getRootId().equals(variableRecord.getRootId())) {
        vr.setValue(variableRecord.getValue());
        return 1;
      }
    }
    return 0;
  }

  public List<VariableRecord> getByType(String jobId, LinkPortType type, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getRootId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public List<VariableRecord> getByPort(String jobId, String portId, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getRootId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public VariableRecord get(String jobId, String portId, LinkPortType type, UUID contextId) {
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getType().equals(type) && vr.getRootId().equals(contextId)) {
        return vr;
      }
    }
    return null;
  }

  public List<VariableRecord> findByJobId(String jobId, LinkPortType type, UUID contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getRootId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public List<VariableRecord> find(UUID contextId) {
    return new ArrayList<>(getVariableRecords(contextId));
  }

  public Collection<VariableRecord> getVariableRecords(UUID contextId) {
    return variableRecordsPerContext.computeIfAbsent(contextId, k -> new LinkedBlockingQueue<>());
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
}
