package org.rabix.engine.model;

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

  public LinkMerge getLinkMerge() {
    return linkMerge;
  }

  public void setLinkMerge(LinkMerge linkMerge) {
    this.linkMerge = linkMerge;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public Object getValue() {
    return value;
  }

  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  public void setNumberOfGlobals(int numberOfGlobals) {
    this.numberOfGlobals = numberOfGlobals;
  }

  public void setNumberOfTimesUpdated(int numberOfTimesUpdated) {
    this.numberOfTimesUpdated = numberOfTimesUpdated;
  }

  @Override
  public String toString() {
    return "VariableRecord [contextId=" + contextId + ", jobId=" + jobId + ", portId=" + portId + ", type=" + type
        + ", value=" + value + ", isWrapped=" + isWrapped + ", numberOfGlobals=" + numberOfGlobals + ", linkMerge=" + linkMerge + "]";
  }

}
