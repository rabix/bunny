package org.rabix.engine.rest.service;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.stub.BackendStub;

public interface SchedulerService {

  void start();
  
  void allocate(Job... jobs);

  boolean stop(Job... jobs);

  void addBackendStub(BackendStub<?, ?, ?> backendStub) throws BackendServiceException;

  void freeBackend(Job rootJob);

  void deallocate(Job job);
  
}
