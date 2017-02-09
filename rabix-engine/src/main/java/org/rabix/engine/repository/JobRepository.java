package org.rabix.engine.repository;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface JobRepository {

  void insert(Job job, UUID groupId);
  
  void update(Job job);
  
  Job get(UUID id);
  
  Set<Job> get();
  
  Set<Job> getByRootId(UUID rootId);
  
  Set<Job> getJobsByGroupId(UUID groupId);
  
}