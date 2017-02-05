package org.rabix.engine.repository;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface JobRepository {

  void insert(Job job);

  void insertToGroup(Job job, UUID groupId);
  
  void update(Job job);

//  void scheduleToBackend(UUID jobId, UUID backendId);
//
//  void unschedule(UUID jobId);

  Job get(UUID id);
  
  Set<Job> get();

  Set<Job> get(UUID... ids);

  Set<Job> getByRootId(UUID rootId);
  
  Set<Job> getByGroupId(UUID groupId);

//  Set<Job> getByBackendId(UUID backendId);
//
//  Set<Job> getUnscheduled();
  
}