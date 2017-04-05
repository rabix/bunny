package org.rabix.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.cache.CacheItem.Action;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.LinkRecord.LinkRecordCacheKey;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.service.CacheService;
import org.rabix.engine.service.LinkRecordService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LinkRecordServiceImpl implements LinkRecordService {

  private CacheService cacheService;
  private LinkRecordRepository linkRecordRepository;
  
  @Inject
  public LinkRecordServiceImpl(LinkRecordRepository linkRecordRepository, CacheService cacheService) {
    this.cacheService = cacheService;
    this.linkRecordRepository = linkRecordRepository;
  }
  
  public void create(LinkRecord link) {
    Cache cache = cacheService.getCache(link.getRootId(), LinkRecord.CACHE_NAME);
    cache.put(link, Action.INSERT);
  }

  public List<LinkRecord> findBySourceJobId(String jobId, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, null, null, null, null, null)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceJobId(jobId, rootId);
    Set<LinkRecord> result = new HashSet<>();
    result.addAll(fromCache);
    result.addAll(fromDB);
    return new ArrayList<>(result);
  }

  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, null, varType, null, null, null)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    ;
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceAndSourceType(jobId, varType, rootId);
    Set<LinkRecord> result = new HashSet<>();
    result.addAll(fromCache);
    result.addAll(fromDB);
    return new ArrayList<>(result);
  }
  
  public List<LinkRecord> findBySource(String jobId, String portId, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, portId, null, null, null, null)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    ;
    List<LinkRecord> fromDB = linkRecordRepository.getBySource(jobId, portId, rootId);
    Set<LinkRecord> result = new HashSet<>();
    result.addAll(fromCache);
    result.addAll(fromDB);
    return new ArrayList<>(result);
  }
  
  public int findBySourceCount(String jobId, String portId, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, portId, null, null, null, null)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    ;
    int countFromDB = linkRecordRepository.getBySourceCount(jobId, portId, rootId);
    return fromCache.size() > countFromDB ? fromCache.size() : countFromDB;
  }
  
  
  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, portId, null, null, null, varType)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    ;
    List<LinkRecord> fromDB = linkRecordRepository.getBySourceAndDestinationType(jobId, portId, varType, rootId);
    Set<LinkRecord> result = new HashSet<>();
    result.addAll(fromCache);
    result.addAll(fromDB);
    return new ArrayList<>(result);
  }

  @Override
  public List<LinkRecord> findBySource(String jobId, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, LinkRecord.CACHE_NAME);
    List<LinkRecord> fromCache = Lists.transform(
        cache.get(new LinkRecordCacheKey(rootId, jobId, null, null, null, null, null)),
        new Function<Cachable, LinkRecord>() {
          @Override
          public LinkRecord apply(Cachable input) {
            return (LinkRecord) input;
          }
        });
    ;
    List<LinkRecord> fromDB = linkRecordRepository.getBySource(jobId, rootId);
    Set<LinkRecord> result = new HashSet<>();
    result.addAll(fromCache);
    result.addAll(fromDB);
    return fromDB;
  }

}
