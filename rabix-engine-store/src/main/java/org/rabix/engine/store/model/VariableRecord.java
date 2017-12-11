package org.rabix.engine.store.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class VariableRecord extends TimestampedModel {

  public final static String CACHE_NAME = "VARIABLE_RECORD";

  private UUID rootId;

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

  public VariableRecord(UUID rootId, String jobId, String portId, LinkPortType type, Object value, LinkMerge linkMerge) {
    this(rootId, jobId, portId, type, value, linkMerge, LocalDateTime.now(), LocalDateTime.now());
  }

  public VariableRecord(UUID rootId, String jobId, String portId, LinkPortType type, Object value, LinkMerge linkMerge, LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super(createdAt, modifiedAt);
    this.jobId = jobId;
    this.portId = portId;
    this.type = type;
    this.value = value;
    this.rootId = rootId;
    this.linkMerge = linkMerge;
  }

  public UUID getRootId() {
    return rootId;
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

  public void setRootId(UUID rootId) {
    this.rootId = rootId;
  }

  public void setNumberOfGlobals(int numberOfGlobals) {
    this.numberOfGlobals = numberOfGlobals;
  }

  public void setNumberOfTimesUpdated(int numberOfTimesUpdated) {
    this.numberOfTimesUpdated = numberOfTimesUpdated;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
    result = prime * result + ((portId == null) ? 0 : portId.hashCode());
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VariableRecord other = (VariableRecord) obj;
    if (jobId == null) {
      if (other.jobId != null)
        return false;
    } else if (!jobId.equals(other.jobId))
      return false;
    if (portId == null) {
      if (other.portId != null)
        return false;
    } else if (!portId.equals(other.portId))
      return false;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "VariableRecord [rootId=" + rootId + ", jobId=" + jobId + ", portId=" + portId + ", type=" + type + ", value=" + value + ", isWrapped=" + isWrapped
        + ", numberOfGlobals=" + numberOfGlobals + ", linkMerge=" + linkMerge + "]";
  }
}
