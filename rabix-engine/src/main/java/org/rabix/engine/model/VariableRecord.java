package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class VariableRecord {

  private String contextId;

  private String jobId;
  private String portId;
  private LinkPortType type;
  private Object value;
  private LinkMerge linkMerge;

  private boolean isWrapped; // is value wrapped into array?
  private int numberOfGlobals; // number of 'global' outputs if node is scattered

  private int numberOfTimesUpdated = 0;
  
  private boolean isDefault = true;
  private Object transform;

  public VariableRecord(String contextId, String jobId, String portId, LinkPortType type, Object value, LinkMerge linkMerge) {  
    this.jobId = jobId;
    this.portId = portId;
    this.type = type;
    this.value = value;
    this.contextId = contextId;
    this.linkMerge = linkMerge;
  }

  public String getContextId() {
    return contextId;
  }
  
  public Object getTransform() {
    return transform;
  }
  
  public void setTransform(Object transform) {
    this.transform = transform;
  }
  

  @SuppressWarnings("unchecked")
  public void addValue(Object value, Integer position) {
    numberOfTimesUpdated++;
    if (isDefault) {
      this.value = null;
      isDefault = false;
    }
    if (this.value == null) {
      if (position == 1) {
        this.value = value;
      } else {
        List<Object> valueList = new ArrayList<>();
        expand(valueList, position);
        valueList.set(position - 1, value);
        this.value = valueList;
        this.isWrapped = true;
      }
    } else {
      if (isWrapped) {
        expand((List<Object>) this.value, position);
        ((List<Object>) this.value).set(position - 1, value);
      } else {
        List<Object> valueList = new ArrayList<>();
        valueList.add(this.value);
        expand(valueList, position);
        valueList.set(position - 1, value);
        this.value = valueList;
        this.isWrapped = true;
      }
    }
  }

  public Object linkMerge() {
    switch (linkMerge) {
    case merge_nested:
      return this.value;
    case merge_flattened:
      return mergeFlatten(this.value);
    default:
      return this.value;
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

  public String getJobId() {
    return jobId;
  }

  public int getNumberOfTimesUpdated() {
    return numberOfTimesUpdated;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getPortId() {
    return portId;
  }

  public void setPortId(String portId) {
    this.portId = portId;
  }

  public LinkPortType getType() {
    return type;
  }

  public void setType(LinkPortType type) {
    this.type = type;
  }

  public Object getValue() {
    if (linkMerge == null) {
      return this.value;
    }
    return linkMerge();
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public boolean isWrapped() {
    return isWrapped;
  }

  public void setWrapped(boolean isWrapped) {
    this.isWrapped = isWrapped;
  }

  public int getNumberOfGlobals() {
    return numberOfGlobals;
  }

  public void setNumberGlobals(int numberOfGlobals) {
    this.numberOfGlobals = numberOfGlobals;
  }

  @Override
  public String toString() {
    return "VariableRecord [contextId=" + contextId + ", jobId=" + jobId + ", portId=" + portId + ", type=" + type
        + ", value=" + value + ", isWrapped=" + isWrapped + ", numberOfGlobals=" + numberOfGlobals + ", linkMerge=" + linkMerge + "]";
  }

}
