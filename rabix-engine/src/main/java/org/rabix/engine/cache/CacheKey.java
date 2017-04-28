package org.rabix.engine.cache;

public interface CacheKey {

  boolean satisfies(CacheKey key);
  
}
