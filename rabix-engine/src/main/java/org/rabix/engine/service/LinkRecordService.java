package org.rabix.engine.service;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.LinkRecord;

import java.util.List;
import java.util.UUID;

public interface LinkRecordService {

  void create(LinkRecord link);

  List<LinkRecord> findBySourceJobId(String jobId, UUID rootId);

  List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, UUID rootId);

  List<LinkRecord> findBySource(String jobId, String portId, UUID rootId);

  List<LinkRecord> findBySourceAndSourceType(String jobId, String portId, LinkPortType varType, UUID rootId);

  int findBySourceCount(String jobId, String portId, UUID rootId);

  List<LinkRecord> findBySource(String jobId, UUID rootId);

  List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, UUID rootId);

  void deleteInputLinks(String sourceId, UUID rootId);

}
