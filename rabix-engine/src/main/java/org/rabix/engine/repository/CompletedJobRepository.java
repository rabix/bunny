package org.rabix.engine.repository;

import java.util.UUID;

import org.rabix.bindings.model.Job;

public interface CompletedJobRepository {

  void insert(Job job);
  
  void insert(Job job, UUID backendId);
  
}