package org.rabix.engine.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
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

  private ConcurrentMap<String, Map<UUID, Map<String, Cache>>> caches = new ConcurrentHashMap<String, Map<UUID, Map<String, Cache>>>();
  
  private JobRecordRepository jobRecordRepository;
  private LinkRecordRepository linkRecordRepository;
  private VariableRecordRepository variableRecordRepository;

  private Configuration configuration;
  private boolean clearCache;
  
  @Inject
  public CacheService(JobRecordRepository jobRecordRepository, LinkRecordRepository linkRecordRepository, VariableRecordRepository variableRecordRepository, Configuration configuration) {
    this.configuration = configuration;
    this.jobRecordRepository = jobRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.variableRecordRepository = variableRecordRepository;
    this.clearCache = configuration.getBoolean("cache.clear", true);
  }
  
  public Cache getCache(UUID rootId, String entity) {
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    
    Map<UUID, Map<String, Cache>> cachesByRoot = caches.get(index);
    if (cachesByRoot == null) {
      cachesByRoot = new HashMap<>();
      caches.put(index, cachesByRoot);
    }
    Map<String, Cache> cachesForRoot = cachesByRoot.get(rootId);
    if (cachesForRoot == null) {
      cachesForRoot = generateCache();
      cachesByRoot.put(rootId, cachesForRoot);
    }
    return cachesForRoot.get(entity);
  }
  
  public void remove(UUID rootId) {
    if (rootId == null) {
      return;
    }
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    Map<UUID, Map<String, Cache>> cachesByRoot = caches.get(index);
    if (cachesByRoot == null) {
      return;
    }
    cachesByRoot.remove(rootId);
  }
  
  public void flush(UUID rootId) {
    if (rootId == null) {
      return;
    }
    String index = Long.toString(EventProcessorDispatcher.dispatch(rootId, getNumberOfEventProcessors()));
    Map<UUID, Map<String, Cache>> cachesByRoot = caches.get(index);
    if (cachesByRoot == null) {
      return;
    }
    Map<String, Cache> cachesForRoot = cachesByRoot.get(rootId);
    if (cachesForRoot == null) {
      return;
    }
    for(Entry<String, Cache> cacheEntry : cachesForRoot.entrySet()) {
      Cache cache = cacheEntry.getValue();
      String cacheName = cacheEntry.getKey();
      if (clearCache) {
        cache.flush(true);
        continue;
      }
      
      switch (cacheName) {
      case LinkRecord.CACHE_NAME:
        cache.flush(true);
        break;
      default:
        cache.flush(false);
      }
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
