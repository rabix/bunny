package org.rabix.engine.model;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CacheKey;

public class LinkRecord implements Cachable {

  public final static String CACHE_NAME = "LINK_RECORD";
  
  private String contextId;
  
  private String sourceJobId;
  private String sourceJobPort;
  private LinkPortType sourceVarType;
  
  private String destinationJobId;
  private String destinationJobPort;
  private LinkPortType destinationVarType;

  private Integer position;
  
  public LinkRecord(String contextId, String sourceJobId, String sourceJobPort, LinkPortType sourceVarType, String destinationJobId, String destinationJobPort, LinkPortType destinationVarType, Integer position) {
    this.position = position;
    this.contextId = contextId;
    this.sourceJobId = sourceJobId;
    this.sourceJobPort = sourceJobPort;
    this.sourceVarType = sourceVarType;
    this.destinationJobId = destinationJobId;
    this.destinationJobPort = destinationJobPort;
    this.destinationVarType = destinationVarType;
  }

  public String getContextId() {
    return contextId;
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
  public String toString() {
    return "LinkRecord [contextId=" + contextId + ", sourceJobId=" + sourceJobId + ", sourceJobPort=" + sourceJobPort + ", sourceVarType=" + sourceVarType + ", destinationJobId=" + destinationJobId + ", destinationJobPort=" + destinationJobPort + ", destinationVarType=" + destinationVarType + ", position=" + position + "]";
  }

  @Override
  public CacheKey getCacheKey() {
    return new LinkRecordCacheKey(this);
  }

  @Override
  public String getCacheEntityName() {
    return CACHE_NAME;
  }
  
  public static class LinkRecordCacheKey implements CacheKey {

    public final String contextId;
    
    public final String sourceJobId;
    public final String sourceJobPort;
    public final LinkPortType sourceVarType;
    
    public final String destinationJobId;
    public final String destinationJobPort;
    public final LinkPortType destinationVarType;
    
    public LinkRecordCacheKey(String contextId, String sourceJobId, String sourceJobPort, LinkPortType sourceVarType, String destinationJobId, String destinationJobPort, LinkPortType destinationVarType) {
      this.contextId = contextId;
      this.sourceJobId = sourceJobId;
      this.sourceJobPort = sourceJobPort;
      this.sourceVarType = sourceVarType;
      this.destinationJobId = destinationJobId;
      this.destinationJobPort = destinationJobPort;
      this.destinationVarType = destinationVarType;
    }

    public LinkRecordCacheKey(LinkRecord linkRecord) {
      this.contextId = linkRecord.contextId;
      this.sourceJobId = linkRecord.sourceJobId;
      this.sourceJobPort = linkRecord.sourceJobPort;
      this.sourceVarType = linkRecord.sourceVarType;
      this.destinationJobId = linkRecord.destinationJobId;
      this.destinationJobPort = linkRecord.destinationJobPort;
      this.destinationVarType = linkRecord.destinationVarType;
    }

    @Override
    public boolean satisfies(CacheKey key) {
      if (key instanceof LinkRecordCacheKey) {
        LinkRecordCacheKey key2 = (LinkRecordCacheKey) key;
        if (!contextId.equals(key2.contextId) && key2.contextId != null) {
          return false;
        }
        if (!sourceJobId.equals(key2.sourceJobId) && key2.sourceJobId != null) {
          return false;
        }
        if (!sourceJobPort.equals(key2.sourceJobPort) && key2.sourceJobPort != null) {
          return false;
        }
        if (!sourceVarType.equals(key2.sourceVarType) && key2.sourceVarType != null) {
          return false;
        }
        if (!destinationJobId.equals(key2.destinationJobId) && key2.destinationJobId != null) {
          return false;
        }
        if (!destinationJobPort.equals(key2.destinationJobPort) && key2.destinationJobPort != null) {
          return false;
        }
        if (!destinationVarType.equals(key2.destinationVarType) && key2.destinationVarType != null) {
          return false;
        }
        return true;
      }
      return false;
    }
    
  }
  
}
