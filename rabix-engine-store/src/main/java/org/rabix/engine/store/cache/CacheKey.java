package org.rabix.engine.store.cache;

public interface CacheKey {

  boolean satisfies(CacheKey key);
  
}
