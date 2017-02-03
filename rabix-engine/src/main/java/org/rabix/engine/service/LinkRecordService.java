package org.rabix.engine.service;

import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.cache.CacheItem.Action;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.LinkRecord.LinkRecordCacheKey;
import org.rabix.engine.repository.LinkRecordRepository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LinkRecordService {

  private CacheService cacheService;
  private LinkRecordRepository linkRecordRepository;
  
  @Inject
  public LinkRecordService(LinkRecordRepository linkRecordRepository, CacheService cacheService) {
    this.cacheService = cacheService;
    this.linkRecordRepository = linkRecordRepository;
  }
  
  public void create(LinkRecord link) {
    Cache cache = cacheService.getCache(link.getRootId(), LinkRecord.CACHE_NAME);
    cache.put(link, Action.INSERT);
  }

  public void delete(String rootId) {
  }
  
  public List<LinkRecord> findBySourceJobId(String jobId, String contextId) {
    Cache cache = cacheService.getCache(contextId, LinkRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new LinkRecordCacheKey(contextId, jobId, null, null, null, null, null));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, LinkRecord>() {
        @Override
        public LinkRecord apply(Cachable input) {
          return (LinkRecord) input;
        }
      });
    }
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceJobId(jobId, contextId);
    for (LinkRecord linkRecord : fromDB) {
      cache.put(linkRecord, Action.UPDATE);
    }
    return fromDB;
  }
  
  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new LinkRecordCacheKey(rootId, jobId, null, varType, null, null, null));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, LinkRecord>() {
        @Override
        public LinkRecord apply(Cachable input) {
          return (LinkRecord) input;
        }
      });
    }
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceAndSourceType(jobId, varType, rootId);
    for (LinkRecord linkRecord : fromDB) {
      cache.put(linkRecord, Action.UPDATE);
    }
    return fromDB;
  }
  
  public List<LinkRecord> findBySource(String jobId, String portId, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new LinkRecordCacheKey(rootId, jobId, portId, null, null, null, null));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, LinkRecord>() {
        @Override
        public LinkRecord apply(Cachable input) {
          return (LinkRecord) input;
        }
      });
    }
    List<LinkRecord> fromDB = linkRecordRepository.getBySource(jobId, portId, rootId);
    for (LinkRecord linkRecord : fromDB) {
      cache.put(linkRecord, Action.UPDATE);
    }
    return fromDB;
  }
  
  public List<LinkRecord> findBySourceAndDestinationType(String jobName, String portId, LinkPortType varType, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new LinkRecordCacheKey(rootId, jobName, portId, null, null, null, varType));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, LinkRecord>() {
        @Override
        public LinkRecord apply(Cachable input) {
          return (LinkRecord) input;
        }
      });
    }
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceAndDestinationType(jobName, portId, varType, rootId);
    for (LinkRecord linkRecord : fromDB) {
      cache.put(linkRecord, Action.UPDATE);
    }
    return fromDB;
  }

}
