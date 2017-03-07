package org.rabix.engine.memory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.service.impl.JobRecordServiceImpl.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class InMemoryJobRecordRepository extends JobRecordRepository {

  private final static Logger logger = LoggerFactory.getLogger(InMemoryJobRecordRepository.class);

  Map<UUID, Map<UUID, JobRecord>> jobRecordsPerRoot;

  @Inject
  public InMemoryJobRecordRepository() {
    this.jobRecordsPerRoot = new ConcurrentHashMap<UUID, Map<UUID, JobRecord>>();
  }

  @Override
  public synchronized int insert(JobRecord jobRecord) {
    Map<UUID, JobRecord> rootJobs = jobRecordsPerRoot.get(jobRecord.getRootId());
    if (rootJobs == null) {
      rootJobs = new HashMap<UUID, JobRecord>();
      jobRecordsPerRoot.put(jobRecord.getRootId(), rootJobs);
    }
    rootJobs.put(jobRecord.getExternalId(), jobRecord);
    return 1;
  }

  @Override
  public synchronized int update(JobRecord jobRecord) {
    Map<UUID, JobRecord> rootJobs = jobRecordsPerRoot.get(jobRecord.getRootId());
    if (rootJobs == null) {
      rootJobs = new HashMap<UUID, JobRecord>();
      jobRecordsPerRoot.put(jobRecord.getRootId(), rootJobs);
    }
    rootJobs.put(jobRecord.getExternalId(), jobRecord);
    return 1;
  }

  @Override
  public synchronized void insertBatch(Iterator<JobRecord> records) {
    while (records.hasNext()) {
      JobRecord jobRecord = records.next();
      insert(jobRecord);
    }
  }

  @Override
  public synchronized void updateBatch(Iterator<JobRecord> records) {
    while (records.hasNext()) {
      JobRecord jobRecord = records.next();
      update(jobRecord);
    }
  }

  @Override
  public synchronized int deleteByStatus(JobState state) {
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
  public synchronized void delete(Set<JobIdRootIdPair> externalIDs) {
    for (JobIdRootIdPair job : externalIDs) {
      jobRecordsPerRoot.get(job.rootId).remove(job.id);
      if (job.id.equals(job.rootId)) {
        jobRecordsPerRoot.remove(job.rootId);
      }
    }
  }

  @Override
  public synchronized List<JobRecord> get(UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    if (recordsPerRoot == null) {
      return new ArrayList<>();
    }
    List<JobRecord> records = new ArrayList<>();
    records.addAll(recordsPerRoot.values());
    return records;
  }

  @Override
  public synchronized JobRecord getRoot(UUID rootId) {
    Map<UUID, JobRecord> recordsPerRoot = jobRecordsPerRoot.get(rootId);
    return recordsPerRoot != null ? recordsPerRoot.get(rootId) : null;
  }

  @Override
  public synchronized JobRecord get(String id, UUID rootId) {
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
  public synchronized List<JobRecord> getByParent(UUID parentId, UUID rootId) {
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
  public synchronized List<JobRecord> getReady(UUID rootId) {
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
  public void updateStatus(UUID rootId, JobState state, Set<JobState> whereStates) {
    Map<UUID, JobRecord> jobs = jobRecordsPerRoot.get(rootId);
    jobs.values().stream().filter(p -> whereStates.contains(p.getState())).forEach(p -> {
      p.setState(state);
    });
  }

  @Override
  public List<JobRecord> get(UUID rootId, Set<JobState> states) {
    Map<UUID, JobRecord> jobs = jobRecordsPerRoot.get(rootId);
    return jobs.values().stream().filter(p -> states.contains(p.getState())).collect(Collectors.toList());
  }

}
