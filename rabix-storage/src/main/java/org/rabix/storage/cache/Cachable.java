package org.rabix.storage.cache;

public interface Cachable {

  CacheKey getCacheKey();
  
  String getCacheEntityName();

}
