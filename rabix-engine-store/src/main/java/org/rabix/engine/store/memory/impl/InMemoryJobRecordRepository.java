package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.repository.JobRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryJobRecordRepository extends JobRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(InMemoryJobRecordRepository.class);

  private Map<UUID, Map<UUID, JobRecord>> jobRecordsPerRoot;

  @Inject
  public InMemoryJobRecordRepository() {
    this.jobRecordsPerRoot = new ConcurrentHashMap<>();
  }

  @Override
  public int insert(JobRecord jobRecord) {
    Map<UUID, JobRecord> rootJobs = jobRecordsPerRoot.computeIfAbsent(jobRecord.getRootId(), k -> new ConcurrentHashMap<>());
    rootJobs.put(jobRecord.getExternalId(), jobRecord);
    return 1;
  }

  @Override
  public int update(JobRecord jobRecord) {
    Map<UUID, JobRecord> rootJobs = jobRecordsPerRoot.computeIfAbsent(jobRecord.getRootId(), k -> new ConcurrentHashMap<>());
    rootJobs.put(jobRecord.getExternalId(), jobRecord);
    return 1;
  }

  @Override
  public void insertBatch(Iterator<JobRecord> records) {
    while (records.hasNext()) {
      JobRecord jobRecord = records.next();
      insert(jobRecord);
    }
  }

  @Override
  public void updateBatch(Iterator<JobRecord> records) {
    while (records.hasNext()) {
      JobRecord jobRecord = records.next();
      update(jobRecord);
    }
  }

  @Override
  public int deleteByStatus(JobRecord.JobState state) {
    int count = 0;
    for (Map<UUID, JobRecord> records : jobRecordsPerRoot.values()) {
      for (Iterator<JobRecord> iterator = records.values().iterator(); iterator.hasNext();) {
        JobRecord jobRecord = iterator.next();
        if (jobRecord.getState().equals(state)) {
          iterator.remove();
          count++;
        }
      }
    }
    return count;
  }

  @Override
  public void delete(UUID id, UUID rootId) {
    Map<UUID, JobRecord> jobRecords = jobRecordsPerRoot.get(rootId);
    if (jobRecords != null) {
      jobRecords.remove(id);
    }

    if (id.equals(rootId)) {
      jobRecordsPerRoot.remove(id);
    }
  }

  @Override
  public List<JobRecord> get(UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    if (recordsPerRoot == null) {
      return new ArrayList<>();
    }
    return recordsPerRoot
            .values()
            .stream()
            .filter(jobRecord -> !jobRecord.getExternalId().equals(rootId))
            .collect(Collectors.toList());
  }

  @Override
  public JobRecord getRoot(UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    return recordsPerRoot != null ? recordsPerRoot.get(rootId) : null;
  }

  @Override
  public JobRecord get(String id, UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    if (recordsPerRoot != null) {
      for (JobRecord job : jobRecordsPerRoot.get(rootId).values()) {
        if (job.getId().equals(id)) {
          return job;
        }
      }
    }
    logger.debug("Failed to find jobRecord {} for root {}", id, rootId);
    return null;
  }

  @Override
  public JobRecord getByExternalId(UUID externalId, UUID rootId) {
    Map<UUID, JobRecord> records = jobRecordsPerRoot.get(rootId);
    if (records != null) {
      return records.get(externalId);
    }
    return null;
  }

  @Override
  public List<JobRecord> getByParent(UUID parentId, UUID rootId) {
    List<JobRecord> jobsByParent = new ArrayList<JobRecord>();
    if (jobRecordsPerRoot.get(rootId) != null) {
      for (JobRecord job : jobRecordsPerRoot.get(rootId).values()) {
        if (job.getParentId() != null && job.getParentId().equals(parentId)) {
          jobsByParent.add(job);
        }
      }
    }
    return jobsByParent;
  }

  @Override
  public List<JobRecord> getReady(UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    if (recordsPerRoot == null) {
      return new ArrayList<>();
    }
    List<JobRecord> readyJobs = new ArrayList<>();
    for (JobRecord job : recordsPerRoot.values()) {
      if (job.isReady()) {
        readyJobs.add(job);
      }
    }
    return readyJobs;
  }

  @Override
  public void updateStatus(UUID rootId, JobRecord.JobState state, Set<JobRecord.JobState> whereStates) {
    Map<UUID, JobRecord> jobs = jobRecordsPerRoot.get(rootId);
    jobs.values().stream().filter(p -> whereStates.contains(p.getState())).forEach(p -> {
      p.setState(state);
    });
  }

  @Override
  public List<JobRecord> get(UUID rootId, Set<JobRecord.JobState> states) {
    Map<UUID, JobRecord> jobs = jobRecordsPerRoot.get(rootId);
    return jobs.values().stream().filter(p -> states.contains(p.getState())).collect(Collectors.toList());
  }

}
