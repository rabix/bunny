package org.rabix.executor.service;

import java.util.Map;

import org.rabix.bindings.model.Job;

public interface CacheService {

  void cache(Job job);
  
  Map<String, Object> find(Job job);
  
  boolean isCacheEnabled();

}
