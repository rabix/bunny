package org.rabix.executor.service;

import java.util.Map;

import org.rabix.bindings.model.Job;

public interface ResultCacheService {

  Map<String, Object> findResultsFromCache(Job job);
  
  Map<String, Object> findResultsFromCachingDir(Job job);

}
