package org.rabix.engine.store.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class LinkRecord extends TimestampedModel {

  public final static String CACHE_NAME = "LINK_RECORD";
  
  private UUID rootId;
  
  private String sourceJobId;
  private String sourceJobPort;
  private LinkPortType sourceVarType;
  
  private String destinationJobId;
  private String destinationJobPort;
  private LinkPortType destinationVarType;

  private Integer position;
  
  public LinkRecord(UUID rootId, String sourceJobId, String sourceJobPort, LinkPortType sourceVarType, String destinationJobId, String destinationJobPort, LinkPortType destinationVarType, Integer position) {
    this(rootId, sourceJobId, sourceJobPort, sourceVarType, destinationJobId, destinationJobPort, destinationVarType, position, LocalDateTime.now(), LocalDateTime.now());
  }

  public LinkRecord(UUID rootId, String sourceJobId, String sourceJobPort, LinkPortType sourceVarType, String destinationJobId, String destinationJobPort, LinkPortType destinationVarType, Integer position, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    super(createdAt, modifiedAt);
    this.position = position;
    this.rootId = rootId;
    this.sourceJobId = sourceJobId;
    this.sourceJobPort = sourceJobPort;
    this.sourceVarType = sourceVarType;
    this.destinationJobId = destinationJobId;
    this.destinationJobPort = destinationJobPort;
    this.destinationVarType = destinationVarType;
  }

  public UUID getRootId() {
    return rootId;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  public String getSourceJobId() {
    return sourceJobId;
  }

  public void setSourceJobId(String sourceJobId) {
    this.sourceJobId = sourceJobId;
  }

  public String getSourceJobPort() {
    return sourceJobPort;
  }

  public void setSourceJobPort(String sourceJobPort) {
    this.sourceJobPort = sourceJobPort;
  }

  public LinkPortType getSourceVarType() {
    return sourceVarType;
  }

  public void setSourceVarType(LinkPortType sourceVarType) {
    this.sourceVarType = sourceVarType;
  }

  public String getDestinationJobId() {
    return destinationJobId;
  }

  public void setDestinationJobId(String destinationJobId) {
    this.destinationJobId = destinationJobId;
  }

  public String getDestinationJobPort() {
    return destinationJobPort;
  }

  public void setDestinationJobPort(String destinationJobPort) {
    this.destinationJobPort = destinationJobPort;
  }

  public LinkPortType getDestinationVarType() {
    return destinationVarType;
  }

  public void setDestinationVarType(LinkPortType destinationVarType) {
    this.destinationVarType = destinationVarType;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((destinationJobId == null) ? 0 : destinationJobId.hashCode());
    result = prime * result + ((destinationJobPort == null) ? 0 : destinationJobPort.hashCode());
    result = prime * result + ((destinationVarType == null) ? 0 : destinationVarType.hashCode());
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + ((sourceJobId == null) ? 0 : sourceJobId.hashCode());
    result = prime * result + ((sourceJobPort == null) ? 0 : sourceJobPort.hashCode());
    result = prime * result + ((sourceVarType == null) ? 0 : sourceVarType.hashCode());
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
    LinkRecord other = (LinkRecord) obj;
    if (destinationJobId == null) {
      if (other.destinationJobId != null)
        return false;
    } else if (!destinationJobId.equals(other.destinationJobId))
      return false;
    if (destinationJobPort == null) {
      if (other.destinationJobPort != null)
        return false;
    } else if (!destinationJobPort.equals(other.destinationJobPort))
      return false;
    if (destinationVarType != other.destinationVarType)
      return false;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (sourceJobId == null) {
      if (other.sourceJobId != null)
        return false;
    } else if (!sourceJobId.equals(other.sourceJobId))
      return false;
    if (sourceJobPort == null) {
      if (other.sourceJobPort != null)
        return false;
    } else if (!sourceJobPort.equals(other.sourceJobPort))
      return false;
    if (sourceVarType != other.sourceVarType)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "LinkRecord [contextId=" + rootId + ", sourceJobId=" + sourceJobId + ", sourceJobPort=" + sourceJobPort + ", sourceVarType=" + sourceVarType + ", destinationJobId=" + destinationJobId + ", destinationJobPort=" + destinationJobPort + ", destinationVarType=" + destinationVarType + ", position=" + position + "]";
  }
}
