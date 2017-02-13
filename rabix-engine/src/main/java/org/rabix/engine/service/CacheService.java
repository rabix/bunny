package org.rabix.engine.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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

  private ConcurrentMap<String, Map<String, Map<String, Cache>>> caches = new ConcurrentHashMap<String, Map<String, Map<String, Cache>>>();
  
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
    
    Map<String, Map<String, Cache>> singleCache = caches.get(index);
    if (singleCache == null) {
      singleCache = generateCache(rootId);
      caches.put(index, singleCache);
    }
    return singleCache.get(rootId).get(entity);
  }
  
  public synchronized void flush(String rootId) {
    if (rootId == null) {
      return;
    }
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    Map<String, Cache> singleCache = caches.get(index).get(rootId);
    if (singleCache == null) {
      return;
    }
    for (Entry<String, Cache> entryCache : singleCache.entrySet()) {
      String key = entryCache.getKey();
      Cache cache = entryCache.getValue();
      cache.flush(key.equals(LinkRecord.CACHE_NAME));
    }
  }
  
  private Map<String, Map<String, Cache>> generateCache(String rootId) {
    Map<String, Map<String, Cache>> generated = new LinkedHashMap<>();
    HashMap<String, Cache> cachesPerRootId = new HashMap<>();
    cachesPerRootId.put(JobRecord.CACHE_NAME, new Cache(jobRecordRepository));
    cachesPerRootId.put(VariableRecord.CACHE_NAME, new Cache(variableRecordRepository));
    cachesPerRootId.put(LinkRecord.CACHE_NAME, new Cache(linkRecordRepository));
    generated.put(rootId, cachesPerRootId);
    return generated;
  }
  
  private int getNumberOfEventProcessors() {
    return configuration.getInt("bunny.event_processor.count", Runtime.getRuntime().availableProcessors());
  }
  
}
