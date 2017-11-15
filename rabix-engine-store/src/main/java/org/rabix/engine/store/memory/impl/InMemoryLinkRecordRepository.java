package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.repository.LinkRecordRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLinkRecordRepository extends LinkRecordRepository {

  private final Map<UUID, List<LinkRecord>> linkRecordRepository;

  @Inject
  public InMemoryLinkRecordRepository() {
    this.linkRecordRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insertBatch(Iterator<LinkRecord> records) {
    while(records.hasNext()) {
      LinkRecord record = records.next();
      insertLinkRecord(record);
    }
  }

  @Override
  public void updateBatch(Iterator<LinkRecord> records) {
  }

  @Override
  public void delete(Set<JobIdRootIdPair> pairs) {

  }

  @Override
  public int insert(LinkRecord linkRecord) {
    insertLinkRecord(linkRecord);
    return 1;
  }

  @Override
  public int update(LinkRecord linkRecord) {
    return 1;
  }

  @Override
  public List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceJobPort().equals(sourceJobPortId)) {
        result.add(lr);
      }
    }
    return result;
  }



  @Override
  public List<LinkRecord> getBySourceJobId(String sourceJobId, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getRootId().equals(rootId)) {
        result.add(lr);
      }
    }
    return result;
  }

  @Override
  public List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceVarType().equals(sourceType)) {
        result.add(lr);
      }
    }
    return result;
  }

  @Override
  public List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(sourceJobId) && lr.getSourceJobPort().equals(sourceJobPortId) && lr.getDestinationVarType().equals(destinationType)) {
        result.add(lr);
      }
    }
    return result;
  }

  private void insertLinkRecord(LinkRecord linkRecord) {
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

  private List<LinkRecord> getLinkRecords(UUID contextId) {
    return linkRecordRepository.computeIfAbsent(contextId, k -> new ArrayList<>());
  }

  @Override
  public List<LinkRecord> getBySourceAndSourceType(String jobId, String portId, LinkPortType varType, UUID rootId) {
    List<LinkRecord> result = new ArrayList<>();
    for (LinkRecord lr : getLinkRecords(rootId)) {
      if (lr.getSourceJobId().equals(jobId) && lr.getSourceJobPort().equals(portId) && lr.getSourceVarType().equals(varType)) {
        result.add(lr);
      }
    }
    return result;
  }

}
