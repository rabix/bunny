package org.rabix.engine.db;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.repository.JobRepository;

import com.google.inject.Inject;

public class JobDB {

  private JobRepository jobRepository;

  @Inject
  public JobDB(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }
  
  public void add(Job job) {
    jobRepository.insert(job, null);
  }
  
  public void add(Job job, UUID groupId) {
    jobRepository.insert(job, groupId);
  }
  
  public void update(Job job) {
    jobRepository.update(job);
  }
  
  public Job get(UUID id) {
    return jobRepository.get(id);
  }
  
  public Set<Job> getJobs() {
    return jobRepository.get();
  }
  
  public Set<Job> getJobs(UUID rootId) {
    return jobRepository.getByRootId(rootId);
  }
  
  public Set<Job> getJobsByGroupId(UUID groupId) {
    return jobRepository.getReadyJobsByGroupId(groupId);
  }
}
