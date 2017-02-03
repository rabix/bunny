package org.rabix.engine.rest.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.transport.backend.Backend;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, UUID rootId) throws JobServiceException;

  Job start(Job job, Map<String, Object> config) throws JobServiceException;
  
  void stop(UUID id) throws JobServiceException;
  
  Set<Job> get();
  
  Job get(UUID id);

}
