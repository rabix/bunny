package org.rabix.engine.lru;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {

  private Map<K, V> cache;
  private int DEFAULT_CACHE_SIZE = 16;
  private int cacheSize;
  private String cacheName;

  public LRUCache(String cacheName) {
    this.cacheName = cacheName;
    this.cacheSize = DEFAULT_CACHE_SIZE;
    cache = new LinkedHashMap<K, V>(DEFAULT_CACHE_SIZE, 1, true);
  }

  public LRUCache(String cacheName, int cacheSize) {
    this.cacheName = cacheName;
    this.cacheSize = cacheSize;
    cache = new LinkedHashMap<K, V>(cacheSize, 1, true);
  }

  public synchronized String getCacheName() {
    return cacheName;
  }
  
  public synchronized int getCacheSize() {
    return cacheSize;
  }

  public synchronized V get(K key) {
    return cache.get(key) != null ? cache.get(key) : null;
  }

  public synchronized void put(K key, V val) {
    if (cacheFull()) {
      remove();
    }
    cache.put(key, val);
  }
  
  public synchronized int size() {
    return cache.size();
  }

  private synchronized boolean cacheFull() {
    return cache.size() == cacheSize;
  }
  
  private synchronized void remove() {
    Iterator<Map.Entry<K, V>> it = cache.entrySet().iterator();
    Map.Entry<K, V> remove = it.next();
    cache.remove(remove.getKey(), remove.getValue());
  }
  
  public synchronized String toString() {
    StringBuffer result =new StringBuffer("Cache: \n");  
    for(Map.Entry<K, V> entry : cache.entrySet()) {
      result.append(entry.getKey() + ": " + entry.getValue() + "\n");
    }
    return result.toString();
  }
  
}
