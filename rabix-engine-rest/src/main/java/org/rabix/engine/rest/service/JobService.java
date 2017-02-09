package org.rabix.engine.rest.service;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException;

  Job start(Job job, Map<String, Object> config) throws JobServiceException;
  
  void stop(String id) throws JobServiceException;
  
  Set<Job> get();
  
  Job get(String id);

}
