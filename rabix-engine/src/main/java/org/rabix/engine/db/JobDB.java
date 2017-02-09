package org.rabix.engine.db;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.SchemaHelper;
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
  
  public void add(Job job, String groupId) {
    jobRepository.insert(job, SchemaHelper.toUUID(groupId));
  }
  
  public void update(Job job) {
    jobRepository.update(job);
  }
  
  public Job get(String id) {
    return jobRepository.get(SchemaHelper.toUUID(id));
  }
  
  public Set<Job> getJobs() {
    return jobRepository.get();
  }
  
  public Set<Job> getJobs(String rootId) {
    return jobRepository.getByRootId(SchemaHelper.toUUID(rootId));
  }
  
  public Set<Job> getJobsByGroupId(String groupId) {
    return jobRepository.getJobsByGroupId(SchemaHelper.toUUID(groupId));
  }
}
