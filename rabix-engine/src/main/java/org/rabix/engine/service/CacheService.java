package org.rabix.engine.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.processor.EventProcessor.EventProcessorDispatcher;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;

import com.google.inject.Inject;

public class CacheService {

  private Map<String, Map<String, Cache>> caches = new HashMap<>();
  
  private JobRecordRepository jobRecordRepository;
  private LinkRecordRepository linkRecordRepository;
  private VariableRecordRepository variableRecordRepository;

  private Configuration configuration;
  
  @Inject
  public CacheService(JobRecordRepository jobRecordRepository, LinkRecordRepository linkRecordRepository, VariableRecordRepository variableRecordRepository, Configuration configuration) {
    this.configuration = configuration;
    this.jobRecordRepository = jobRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.variableRecordRepository = variableRecordRepository;
  }
  
  public Cache getCache(String rootId, String entity) {
    String index = "" + EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors());
    
    Map<String, Cache> singleCache = caches.get(index);
    if (singleCache == null) {
      singleCache = generateCache();
      caches.put(index, singleCache);
    }
    return singleCache.get(entity);
  }
  
  public void flush(String rootId) {
    if (rootId == null) {
      return;
    }
    String index = "" + EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors());
    Map<String, Cache> singleCache = caches.get(index);
    if (singleCache == null) {
      return;
    }
    for (Cache cache : singleCache.values()) {
      cache.flush();
    }
  }
  
  private Map<String, Cache> generateCache() {
    Map<String, Cache> generated = new HashMap<>();
    generated.put("JOB_RECORD", new Cache(jobRecordRepository));
    generated.put("LINK_RECORD", new Cache(linkRecordRepository));
    generated.put("VARIABLE_RECORD", new Cache(variableRecordRepository));
    return generated;
  }
  
  private int getNumberOfEventProcessors() {
    return configuration.getInt("bunny.event_processor.count", Runtime.getRuntime().availableProcessors());
  }
  
}
