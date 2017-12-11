package org.rabix.engine.service;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.JobRecord;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JobRecordService {

  public static UUID generateUniqueId() {
    return UUID.randomUUID();
  }

  void create(JobRecord jobRecord);

  void delete(UUID rootId);

  void update(JobRecord jobRecord);

  List<JobRecord> findReady(UUID rootId);

  List<JobRecord> findByParent(UUID parentId, UUID rootId);

  JobRecord find(String id, UUID rootId);

  JobRecord findByExternalId(UUID externalId, UUID rootId);

  List<JobRecord> find(UUID rootId, Set<JobRecord.JobState> statuses);

  JobRecord findRoot(UUID rootId);

  void increaseInputPortIncoming(JobRecord jobRecord, String port);

  void increaseOutputPortIncoming(JobRecord jobRecord, String port);

  void incrementPortCounter(JobRecord jobRecord, DAGLinkPort port, LinkPortType type);

  void decrementPortCounter(JobRecord jobRecord, String portId, LinkPortType type);

  void resetInputPortCounters(JobRecord jobRecord, int value);

  void resetInputPortCounter(JobRecord jobRecord, int value, String port);

  void resetOutputPortCounter(JobRecord jobRecord, int value, String port);

  void resetOutputPortCounters(JobRecord jobRecord, int value);

}
