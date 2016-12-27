package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.dao.VariableRecordRepository;
import org.rabix.engine.model.VariableRecord;

import com.google.inject.Inject;

public class VariableRecordService {

  private VariableRecordRepository variableRecordRepository;

  @Inject
  public VariableRecordService(VariableRecordRepository variableRecordRepository) {
    this.variableRecordRepository = variableRecordRepository;
  }
  
  public void create(VariableRecord variableRecord) {
    variableRecordRepository.insert(variableRecord);
  }
  
  public void delete(String rootId) {
//    variableRecordsPerContext.remove(rootId);
  }

  public void update(VariableRecord variableRecord) {
    variableRecordRepository.update(variableRecord);
  }
  
  public List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    return variableRecordRepository.getByType(jobId, type, contextId);
  }
  
  public List<VariableRecord> find(String jobId, String portId, String contextId) {
    return variableRecordRepository.getByPort(jobId, portId, contextId);
  }

  public VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    return variableRecordRepository.get(jobId, portId, type, contextId);
  }

  @SuppressWarnings("unchecked")
  public void addValue(VariableRecord variableRecord, Object value, Integer position, boolean isScatterWrapper) {
    variableRecord.setNumberOfTimesUpdated(variableRecord.getNumberOfTimesUpdated());

    if (variableRecord.isDefault()) {
      variableRecord.setValue(null);
      variableRecord.setDefault(false);
    }
    if (variableRecord.getValue() == null) {
      if (position == 1) {
        if (isScatterWrapper) {
          variableRecord.setValue(new ArrayList<>());
          ((ArrayList<Object>) variableRecord.getValue()).add(value);
        } else {
          variableRecord.setValue(value);
        }
      } else {
        List<Object> valueList = new ArrayList<>();
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    } else {
      if (variableRecord.isWrapped()) {
        expand((List<Object>) variableRecord.getValue(), position);
        ((List<Object>) variableRecord.getValue()).set(position - 1, value);
      } else {
        List<Object> valueList = new ArrayList<>();
        valueList.add(variableRecord.getValue());
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    }
  }

  public Object linkMerge(VariableRecord variableRecord) {
    switch (variableRecord.getLinkMerge()) {
    case merge_nested:
      return variableRecord.getValue();
    case merge_flattened:
      return mergeFlatten(variableRecord.getValue());
    default:
      return variableRecord.getValue();
    }
  }

  private <T> void expand(List<T> list, Integer position) {
    int initialSize = list.size();
    if (initialSize >= position) {
      return;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return;
  }

  @SuppressWarnings("unchecked")
  private Object mergeFlatten(Object value) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof List<?>)) {
      return value;
    }
    List<Object> flattenedValues = new ArrayList<>();
    if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        Object flattenedSubvalue = mergeFlatten(subvalue);
        if (flattenedSubvalue instanceof List<?>) {
          flattenedValues.addAll((Collection<? extends Object>) flattenedSubvalue);
        } else {
          flattenedValues.add(flattenedSubvalue);
        }
      }
    } else {
      flattenedValues.add(value);
    }
    return flattenedValues;
  }
  
  public Object getValue(VariableRecord variableRecord) {
    if (variableRecord.getLinkMerge() == null) {
      return variableRecord.getValue();
    }
    return linkMerge(variableRecord);
  }
  
}
