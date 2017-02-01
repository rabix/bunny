package org.rabix.engine.rest.service;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.stub.BackendStub;

public interface SchedulerService {

  void send(Job... jobs);

  boolean stop(Job... jobs);

  void addBackendStub(BackendStub<?, ?, ?> backendStub);

  void freeBackend(Job rootJob);

  void remove(Job job);
  
}
