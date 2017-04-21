package org.rabix.engine.service;

import java.util.UUID;

import org.rabix.engine.cache.Cache;

public interface CacheService {

  Cache getCache(UUID rootId, String entity);
  
  void flush(UUID rootId);
  
  void clear(UUID rootId);
  
}
