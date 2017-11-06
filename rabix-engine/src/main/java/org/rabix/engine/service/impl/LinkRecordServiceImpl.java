package org.rabix.engine.service.impl;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.repository.LinkRecordRepository;

import com.google.inject.Inject;

public class LinkRecordServiceImpl implements LinkRecordService {

  private LinkRecordRepository linkRecordRepository;

  @Inject
  public LinkRecordServiceImpl(LinkRecordRepository linkRecordRepository) {
    this.linkRecordRepository = linkRecordRepository;
  }

  public void create(LinkRecord link) {
    linkRecordRepository.insert(link);
  }

  public List<LinkRecord> findBySourceJobId(String jobId, UUID rootId) {
    return linkRecordRepository.getBySourceJobId(jobId, rootId);
  }

  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, UUID rootId) {
    return linkRecordRepository.getBySourceAndSourceType(jobId, varType, rootId);
  }

  public List<LinkRecord> findBySource(String jobId, String portId, UUID rootId) {
    return linkRecordRepository.getBySource(jobId, portId, rootId);
  }

  public int findBySourceCount(String jobId, String portId, UUID rootId) {
    return linkRecordRepository.getBySourceCount(jobId, portId, rootId);
  }


  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, UUID rootId) {
    return linkRecordRepository.getBySourceAndDestinationType(jobId, portId, varType, rootId);
  }

  @Override
  public List<LinkRecord> findBySource(String jobId, UUID rootId) {
    return linkRecordRepository.getBySource(jobId, rootId);
  }

  @Override
  public List<LinkRecord> findBySourceAndSourceType(String jobId, String portId, LinkPortType varType, UUID rootId) {
    return linkRecordRepository.getBySourceAndSourceType(jobId, portId, varType, rootId);
  }

}
