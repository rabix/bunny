package org.rabix.engine.repository;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.LinkRecord;

public interface LinkRecordRepository {

  int insert(LinkRecord linkRecord);
  
  int update(LinkRecord linkRecord);
  
  List<LinkRecord> getBySource(String sourceJobId, String sourceJobPortId, String rootId);
  
  List<LinkRecord> getBySourceJobId(String sourceJobId, String rootId);
  
  List<LinkRecord> getBySourceAndSourceType(String sourceJobId, LinkPortType sourceType, String rootId);
  
  List<LinkRecord> getBySourceAndDestinationType(String sourceJobId, String sourceJobPortId, LinkPortType destinationType, String rootId);
  
}
