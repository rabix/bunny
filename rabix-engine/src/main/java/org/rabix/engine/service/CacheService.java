package org.rabix.engine.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor.EventProcessorDispatcher;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;

import com.google.inject.Inject;

public class CacheService {

  private ConcurrentMap<String, Map<String, Cache>> caches = new ConcurrentHashMap<String, Map<String, Cache>>();
  
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
  
  public synchronized Cache getCache(String rootId, String entity) {
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    
    Map<String, Cache> singleCache = caches.get(index);
    if (singleCache == null) {
      singleCache = generateCache();
      caches.put(index, singleCache);
    }
    return singleCache.get(entity);
  }
  
  public synchronized void flush(String rootId) {
    if (rootId == null) {
      return;
    }
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    Map<String, Cache> singleCache = caches.get(index);
    if (singleCache == null) {
      return;
    }
    for (Cache cache : singleCache.values()) {
      cache.flush();
    }
  }
  
  private Map<String, Cache> generateCache() {
    Map<String, Cache> generated = new LinkedHashMap<>();
    generated.put(JobRecord.CACHE_NAME, new Cache(jobRecordRepository));
    generated.put(VariableRecord.CACHE_NAME, new Cache(variableRecordRepository));
    generated.put(LinkRecord.CACHE_NAME, new Cache(linkRecordRepository));
    return generated;
  }
  
  private int getNumberOfEventProcessors() {
    return configuration.getInt("bunny.event_processor.count", Runtime.getRuntime().availableProcessors());
  }
  
}
