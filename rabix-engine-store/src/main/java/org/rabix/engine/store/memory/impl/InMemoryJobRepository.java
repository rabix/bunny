package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.store.repository.JobRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryJobRepository implements JobRepository {

  private final Map<UUID, Map<UUID, JobEntity>> jobRepository;

  @Inject
  public InMemoryJobRepository() {
    this.jobRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(Job job, UUID groupId, String producedByNode) {
    if (job == null) {
      return;
    }

    Map<UUID, JobEntity> rootJobs = jobRepository.get(job.getRootId());
    if(rootJobs == null) {
      rootJobs = new HashMap<>();
      rootJobs.put(job.getId(), new JobEntity(job, groupId, producedByNode));
      jobRepository.put(job.getRootId(), rootJobs);
    } else {
      rootJobs.put(job.getId(), new JobEntity(job, groupId, producedByNode));
    }
  }

  @Override
  public void update(Job job) {
    if (job == null) {
      return;
    }
    Map<UUID, JobEntity> rootJobs = jobRepository.get(job.getRootId());
    if (rootJobs == null) {
      return;
    }

    JobEntity jobEntity = rootJobs.get(job.getId());
    if (jobEntity != null) {
      jobEntity.setJob(job);
    }
  }

  @Override
  public void updateBackendId(UUID jobId, UUID backendId) {
    JobEntity jobEntity = getJobEntity(jobId);
    if(jobEntity != null) {
      jobEntity.setBackendId(backendId);
    }
  }

  @Override
  public void dealocateJobs(UUID backendId) {
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(backendId.equals(job.getBackendId()) && job.getJob().getStatus().equals(JobStatus.READY)) {
          job.setBackendId(null);
        }
      }
    }
  }

  @Override
  public Job get(UUID id) {
    JobEntity jobEntity = getJobEntity(id);
    return jobEntity != null ? jobEntity.getJob() : null;
  }

  @Override
  public Set<Job> get() {
    Set<Job> allJobs = new HashSet<>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        allJobs.add(job.getJob());
      }
    }
    return allJobs;
  }

  @Override
  public Set<Job> getByRootId(UUID rootId) {
    Set<Job> rootJobs = new HashSet<>();
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    for(JobEntity job: jobs.values()) {
      rootJobs.add(job.getJob());
    }
    return rootJobs;
  }

  @Override
  public Set<UUID> getBackendsByRootId(UUID rootId) {
    Set<UUID> backends = new HashSet<>();
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    for(JobEntity job: jobs.values()) {
      backends.add(job.getBackendId());
    }
    return backends;
  }

  @Override
  public Set<Job> getReadyJobsByGroupId(UUID groupId) {
    Set<Job> groupIdJobs = new HashSet<>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(job.getGroupId() != null && job.getGroupId().equals(groupId) && !job.getJob().isRoot()) {
          groupIdJobs.add(job.getJob());
        }
      }
    }
    return groupIdJobs;
  }

  @Override
  public Set<JobEntity> getReadyFree() {
    Set<JobEntity> readyFreeJobs = new HashSet<>();
    for(Map<UUID, JobEntity> rootJobs: jobRepository.values()) {
      for(JobEntity job: rootJobs.values()) {
        if(job.getBackendId() == null && job.getJob().getStatus().equals(JobStatus.READY)) {
          readyFreeJobs.add(job);
        }
      }
    }
    return readyFreeJobs;
  }

  @Override
  public void updateBackendIds(Iterator<JobEntity> entities) {
    while(entities.hasNext()) {
      JobEntity jbp = entities.next();
      updateBackendId(jbp.getJob().getId(), jbp.getBackendId());
    }
  }

  @Override
  public UUID getBackendId(UUID jobId) {
    JobEntity jobEntity = getJobEntity(jobId);
    return jobEntity != null ? jobEntity.getBackendId() : null;

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

  @Override
  public void update(Iterator<Job> jobs) {
    while(jobs.hasNext()){
      Job next = jobs.next();
      jobRepository.get(next.getRootId()).get(next.getId()).setJob(next);
    }
  }

  @Override
  public void updateStatus(UUID rootId, JobStatus status, Set<JobStatus> statuses) {
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    jobs.values().stream().filter(p -> statuses.contains(p.getJob().getStatus()))
        .forEach(p -> p.setJob(Job.cloneWithStatus(p.getJob(), status)));
  }

  @Override
  public Set<Job> get(UUID rootId, Set<JobStatus> statuses) {
    Map<UUID, JobEntity> jobs = jobRepository.get(rootId);
    return jobs.values().stream().filter(p -> statuses.contains(p.getJob().getStatus())).map(JobEntity::getJob).collect(Collectors.toSet());
  }

  @Override
  public Set<Job> getRootJobsForDeletion(JobStatus status, Timestamp time) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteByRootIds(Set<UUID> rootIds) {
    // TODO Auto-generated method stub
  }

}
