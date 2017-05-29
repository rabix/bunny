package org.rabix.storage.lru.app;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Application;
import org.rabix.storage.lru.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class AppCache extends LRUCache<String, Application> {
  
  private final Logger logger = LoggerFactory.getLogger(AppCache.class);
  public static final String CACHE_NAME = "ApplicationCache";
  private static int DEFAULT_CACHE_SIZE = 16;
  
  @Inject
  public AppCache(Configuration configuration) {
    super(CACHE_NAME, configuration.getInteger("cache.application.size", DEFAULT_CACHE_SIZE));
    logger.debug("{} initialized with size={}", CACHE_NAME, getCacheSize());
  }
  
}
