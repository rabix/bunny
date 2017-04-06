package org.rabix.engine.memory.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.repository.LinkRecordRepository;

import com.google.inject.Inject;

public class InMemoryLinkRecordRepository extends LinkRecordRepository {

  Map<UUID, List<LinkRecord>> linkRecordRepository;
  
  @Inject
  public InMemoryLinkRecordRepository() {
    this.linkRecordRepository = new ConcurrentHashMap<UUID, List<LinkRecord>>();
  }

  @Override
  public synchronized void insertBatch(Iterator<LinkRecord> records) {
    while(records.hasNext()) {
      LinkRecord record = records.next();
      insertLinkRecord(record);
    }
  }

  @Override
  public synchronized void updateBatch(Iterator<LinkRecord> records) {
    while(records.hasNext()) {
      LinkRecord record = records.next();
      updateLinkRecord(record);
    }
  }

  @Override
  public synchronized void delete(Set<JobIdRootIdPair> pairs) {
    
  }

  @Override
  public synchronized int insert(LinkRecord linkRecord) {
    insertLinkRecord(linkRecord);
    return 1;
  }

  @Override 
  public synchronized int update(LinkRecord linkRecord) {
    updateLinkRecord(linkRecord);
    return 1;
  }

  @Override
  public synchronized List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceJobPort().equals(sourceJobPortId)) {
        result.add(lr);
      }
    }
    return result;
  }

  
  
  @Override
  public synchronized List<LinkRecord> getBySourceJobId(String sourceJobId, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getRootId().equals(rootId)) {
        result.add(lr);
      }
    }
    return result;
  }

  @Override
  public synchronized List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceVarType().equals(sourceType)) {
        result.add(lr);
      }
    }
    return result;
  }

  @Override
  public synchronized List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceJobPort().equals(sourceJobPortId) && lr.getDestinationVarType().equals(destinationType)) {
        result.add(lr);
      }
    }
    return result;
  }
  
  private synchronized void insertLinkRecord(LinkRecord linkRecord) {
    getLinkRecords(linkRecord.getRootId()).add(linkRecord);
  }
  

  @Override
  public List<LinkRecord> getBySource(String sourceJobId, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    List<LinkRecord> links = getLinkRecords(rootId);
    for(LinkRecord link: links) {
      if(link.getSourceJobId().equals(sourceJobId)) {
        result.add(link);
      }
    }
    return new ArrayList<>(result);
  }
  
  @Override
  public int getBySourceCount(String sourceJobId, String sourceJobPortId, UUID rootId) {
    return getBySource(sourceJobId, sourceJobPortId, rootId).size();
  }
  
  private synchronized void updateLinkRecord(LinkRecord linkRecord) {
//    List<LinkRecord> links = linkRecordRepository.get(linkRecord.getRootId());
//    for (Iterator<LinkRecord> iterator = links.iterator(); iterator.hasNext();) {
//      LinkRecord link = iterator.next();
//      if(link.getRootId().equals(linkRecord.getRootId()) && 
//          link.getSourceJobId().equals(linkRecord.getSourceJobId()) && 
//          link.getSourceJobPort().equals(linkRecord.getSourceJobPort()) &&
//          link.getSourceVarType().equals(linkRecord.getSourceVarType()) &&
//          link.getDestinationJobId().equals(linkRecord.getDestinationJobId()) &&
//          link.getDestinationJobPort().equals(linkRecord.getDestinationJobPort()) &&
//          link.getDestinationVarType().equals(linkRecord.getDestinationVarType())) {
//        iterator.remove();
//        break;
//      }
//    }
//    links.add(linkRecord);
  }
  
  private List<LinkRecord> getLinkRecords(UUID contextId) {
    List<LinkRecord> linkList = linkRecordRepository.get(contextId);
    if (linkList == null) {
      linkList = new ArrayList<>();
      linkRecordRepository.put(contextId, linkList);
    }
    return linkList;
  }
  
}
