package org.rabix.engine.store.repository;

import org.rabix.engine.store.model.JobRecord;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class JobRecordRepository {

  public abstract int insert(JobRecord jobRecord);

  public abstract int update(JobRecord jobRecord);

  public abstract void insertBatch(Iterator<JobRecord> records);

  public abstract void updateBatch(Iterator<JobRecord> records);

  public abstract void delete(UUID externalId, UUID rootId);

  public abstract List<JobRecord> get(UUID rootId);

  public abstract JobRecord getRoot(UUID rootId);

  public abstract JobRecord get(String id, UUID rootId);

  public abstract JobRecord getByExternalId(UUID externalId, UUID rootId);

  public abstract List<JobRecord> getByParent(UUID parentId, UUID rootId);

  public abstract List<JobRecord> getReady(UUID rootId);

  public abstract void updateStatus(UUID rootId, JobRecord.JobState state, Set<JobRecord.JobState> whereStates);

  public abstract List<JobRecord> get(UUID rootId, Set<JobRecord.JobState> states);

}
