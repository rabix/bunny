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

  public String getCacheName() {
    return cacheName;
  }

  public V get(K key) {
    return cache.get(key) != null ? cache.get(key) : null;
  }

  public void put(K key, V val) {
    if (cacheFull()) {
      remove();
    }
    cache.put(key, val);
  }

  private boolean cacheFull() {
    return cache.size() == cacheSize;
  }

  private void remove() {
    Iterator<Map.Entry<K, V>> it = cache.entrySet().iterator();
    Map.Entry<K, V> remove = it.next();
    cache.remove(remove.getKey(), remove.getValue());
  }
  
  public String toString() {
    StringBuffer result =new StringBuffer("Cache: \n");  
    for(Map.Entry<K, V> entry : cache.entrySet()) {
      result.append(entry.getKey() + ": " + entry.getValue() + "\n");
    }
    return result.toString();
  }
  
  
  public static void main(String[] args) {
    LRUCache<String, Integer> cache = new LRUCache<String, Integer>("Test", 8);
    cache.put("1", 1);
    cache.put("2", 2);
    cache.put("3", 3);
    cache.put("4", 4);
    cache.get("2");
    cache.get("3");
    cache.get("4");
    cache.get("2");
    cache.put("5", 5);
    System.out.println(cache);
  }
  
}
