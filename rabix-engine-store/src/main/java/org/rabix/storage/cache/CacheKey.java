package org.rabix.storage.cache;

public interface CacheKey {

  boolean satisfies(CacheKey key);
  
}
