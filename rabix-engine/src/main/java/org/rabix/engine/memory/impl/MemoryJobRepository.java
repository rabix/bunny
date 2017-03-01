package org.rabix.engine.memory.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.jdbi.impl.JDBIJobRepository.JobBackendPair;
import org.rabix.engine.repository.JobRepository;

import com.google.inject.Inject;

public class MemoryJobRepository implements JobRepository {

  Map<UUID, Map<UUID, JobEntity>> jobRepository;
  
  @Inject
  public MemoryJobRepository() {
    this.jobRepository = new ConcurrentHashMap<UUID, Map<UUID, JobEntity>>();
  }

  @Override
  public synchronized void insert(Job job, UUID groupId) {
    Map<UUID, JobEntity> rootJobs = jobRepository.get(job.getRootId());
    if(rootJobs == null) {
      rootJobs = new HashMap<UUID, JobEntity>();
      rootJobs.put(job.getId(), new JobEntity(job, groupId));
      jobRepository.put(job.getRootId(), rootJobs);
    }
    else {
      rootJobs.put(job.getId(), new JobEntity(job, groupId));
    }
  }

  @Override
  public synchronized void update(Job job) {
    Map<UUID, JobEntity> rootJobs = jobRepository.get(job.getRootId());
    rootJobs.get(job.getId()).setJob(job);
  }

  @Override
  public synchronized void updateBackendId(UUID jobId, UUID backendId) {
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      if(rootJobs.get(jobId) != null) {
         rootJobs.get(jobId).setBackendId(backendId);
      }
    }
  }

  @Override
  public synchronized void dealocateJobs(UUID backendId) {
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(job.getBackendId().equals(backendId) && job.getJob().getStatus().equals(JobStatus.READY)) {
          job.setBackendId(null);
        }
      }
    }
  }

  @Override
  public synchronized Job get(UUID id) {
    return getJobEntity(id) != null ? getJobEntity(id).getJob(): null;
  }

  @Override
  public synchronized Set<Job> get() {
    Set<Job> allJobs = new HashSet<Job>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        allJobs.add(job.getJob());
      }
    }
    return allJobs;
  }

  @Override
  public synchronized Set<Job> getByRootId(UUID rootId) {
    Set<Job> rootJobs = new HashSet<Job>();
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    for(JobEntity job: jobs.values()) {
      rootJobs.add(job.getJob());
    }
    return rootJobs;
  }

  @Override
  public synchronized Set<UUID> getBackendsByRootId(UUID rootId) {
    Set<UUID> backends = new HashSet<UUID>();
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    for(JobEntity job: jobs.values()) {
      backends.add(job.getBackendId());
    }
    return backends;
  }

  @Override
  public synchronized Set<Job> getReadyJobsByGroupId(UUID groupId) {
    Set<Job> groupIdJobs = new HashSet<Job>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(job.getGroupId() != null && job.getGroupId().equals(groupId)) {
          groupIdJobs.add(job.getJob());
        }
      }
    }
    return groupIdJobs;
  }

  @Override
  public synchronized Set<Job> getReadyFree() {
    Set<Job> readyFreeJobs = new HashSet<Job>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(job.getBackendId() == null && job.getJob().getStatus().equals(JobStatus.READY)) {
          readyFreeJobs.add(job.getJob());
        }
      }
    }
    return readyFreeJobs;
  }

  @Override
  public void updateBackendIds(Iterator<JobBackendPair> jobBackendPair) {
    
  }

  @Override
  public UUID getBackendId(UUID jobId) {
    return getJobEntity(jobId) != null ? getJobEntity(jobId).getBackendId() : null;
    
  }

  @Override
  public JobStatus getStatus(UUID id) {
    return getJobEntity(id) != null ? getJobEntity(id).getJob().getStatus() : null;
  }
  
  private JobEntity getJobEntity(UUID jobId) {
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      if(rootJobs.get(jobId) != null) {
        return rootJobs.get(jobId);
      }
    }
    return null;
  }
  
}
