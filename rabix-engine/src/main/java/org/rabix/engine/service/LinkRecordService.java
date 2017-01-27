package org.rabix.engine.service;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.repository.LinkRecordRepository;

import com.google.inject.Inject;

public class LinkRecordService {

  private LinkRecordRepository linkRecordRepository;
  
  @Inject
  public LinkRecordService(LinkRecordRepository linkRecordRepository) {
    this.linkRecordRepository = linkRecordRepository;
  }
  
  public void create(LinkRecord link) {
    linkRecordRepository.insert(link);
  }

  public void delete(String rootId) {
  }
  
  public List<LinkRecord> findBySourceJobId(String jobId, String contextId) {
    return linkRecordRepository.getBySourceJobId(jobId, contextId);
  }
  
  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, String contextId) {
    return linkRecordRepository.getBySourceAndSourceType(jobId, varType, contextId);
  }
  
  public List<LinkRecord> findBySource(String jobId, String portId, String contextId) {
    return linkRecordRepository.getBySource(jobId, portId, contextId);
  }
  
  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, String contextId) {
    return linkRecordRepository.getBySourceAndDestinationType(jobId, portId, varType, contextId);
  }

}
