package org.rabix.engine.service;

import java.util.UUID;

import org.rabix.engine.cache.Cache;

public interface CacheService {

  Cache getCache(UUID rootId, String entity);
  
  void remove(UUID rootId);
  
  void flush(UUID rootId);
  
}
