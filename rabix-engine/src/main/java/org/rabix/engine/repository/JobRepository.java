package org.rabix.engine.repository;

import java.util.Set;

import org.rabix.bindings.model.Job;

public interface JobRepository {

  void insert(String id, String rootId, String job, String groupId);
  
  void update(String id, String job);
  
  Job get(String id);
  
  Set<Job> get();
  
  Set<Job> getByRootId(String rootId);
  
  Set<Job> getJobsByGroupId(String group_id);
  
}