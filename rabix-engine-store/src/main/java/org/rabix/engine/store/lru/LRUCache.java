package org.rabix.engine.store.lru;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class LRUCache<K, V> {

  private static final int DEFAULT_CACHE_SIZE = 1000;

  private Cache<K, V> cache;

  private int cacheSize;
  private String cacheName;

  public LRUCache(String cacheName) {
    this(cacheName, DEFAULT_CACHE_SIZE);
  }

  public LRUCache(String cacheName, int cacheSize) {
    this.cacheName = cacheName;
    this.cacheSize = cacheSize;
    this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).expireAfterAccess(3, TimeUnit.DAYS).build();
  }

  public String getCacheName() {
    return cacheName;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public V get(K key) {
    return cache.getIfPresent(key);
  }

  public void put(K key, V val) {
    cache.put(key, val);
  }

  public int size() {
    return (int) cache.size();
  }

  public String toString() {
    return cache.toString();
  }
}
