package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.LinkRecord;

public class LinkRecordService {

  private ConcurrentMap<String, List<LinkRecord>> linkRecordsPerContext = new ConcurrentHashMap<String, List<LinkRecord>>();

  public void create(LinkRecord link) {
    getLinkRecords(link.getContextId()).add(link);
  }

  public void delete(String rootId) {
    linkRecordsPerContext.remove(rootId);
  }
  
  public List<LinkRecord> findBySourceJobId(String jobId, String contextId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(contextId)) {
      if (lr.getSourceJobId().equals(jobId) && lr.getContextId().equals(contextId)) {
        result.add(lr);
      }
    }
    return result;
  }
  
  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, String contextId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(contextId)) {
      if (lr.getSourceJobId().equals(jobId) && lr.getSourceVarType().equals(varType) && lr.getContextId().equals(contextId)) {
        result.add(lr);
      }
    }
    return result;
  }
  
  public List<LinkRecord> findBySource(String jobId, String portId, String contextId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(contextId)) {
      if (lr.getSourceJobId().equals(jobId) && lr.getSourceJobPort().equals(portId) && lr.getContextId().equals(contextId)) {
        result.add(lr);
      }
    }
    return result;
  }
  
  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, String contextId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(contextId)) {
      if (lr.getSourceJobId().equals(jobId) && lr.getSourceJobPort().equals(portId) && lr.getDestinationVarType().equals(varType) && lr.getContextId().equals(contextId)) {
        result.add(lr);
      }
    }
    return result;
  }

  public List<LinkRecord> find(String contextId) {
    return getLinkRecords(contextId);
  }
  
  private List<LinkRecord> getLinkRecords(String contextId) {
    List<LinkRecord> linkList = linkRecordsPerContext.get(contextId);
    if (linkList == null) {
      linkList = new ArrayList<>();
      linkRecordsPerContext.put(contextId, linkList);
    }
    return linkList;
  }
  
}
