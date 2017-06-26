package org.rabix.engine.store.cache;

public interface Cachable {

  CacheKey getCacheKey();
  
  String getCacheEntityName();

}
