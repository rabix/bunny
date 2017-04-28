package org.rabix.engine.cache;

public interface Cachable {

  CacheKey getCacheKey();
  
  String getCacheEntityName();

}
