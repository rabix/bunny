package org.rabix.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.engine.cache.CacheItem.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache {

  private final static Logger logger = LoggerFactory.getLogger(Cache.class);

  private CachableRepository repository;

  private int hits = 0;
  private ConcurrentMap<CacheKey, CacheItem> cache = new ConcurrentHashMap<>();

  public Cache(CachableRepository repository) {
    this.repository = repository;
  }

  public synchronized void flush(boolean clear) {
    if (cache.isEmpty()) {
      return;
    }
    Collection<CacheItem> items = cache.values();

    List<Cachable> inserts = new ArrayList<>();
    List<Cachable> updates = new ArrayList<>();

    int size = 0;
    for (CacheItem item : items) {
      switch (item.action) {
      case INSERT:
        inserts.add(item.cachable);
        size++;
        break;
      case UPDATE:
        updates.add(item.cachable);
        size++;
        break;
      default:
        break;
      }
    }

    repository.insertCachables(inserts);
    repository.updateCachables(updates);
    logger.debug("{} flushed {} item(s). Cache hits {}.", repository, size, hits);
    
    hits = 0;
    if (clear) {
      cache.clear();
    } else {
      Iterator<Entry<CacheKey, CacheItem>> iterator = cache.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<CacheKey, CacheItem> entry = iterator.next();
        if (!entry.getValue().isHit) {
          iterator.remove();
        } else {
          entry.getValue().isHit = false;
          entry.getValue().action = Action.NOOP;
        }
      }
    }
  }
  
  public synchronized void put(Cachable cachable, Action action) {
    CacheKey key = cachable.getCacheKey();
    if (cache.containsKey(key)) {
      CacheItem item = cache.get(key);
      item.cachable = cachable;
      item.hit();
    } else {
      cache.put(key, new CacheItem(action, cachable));
    }
  }

  public synchronized <T extends Cachable> List<T> merge(List<T> cachables, Class<T> clazz) {
    if (cachables == null) {
      return null;
    }
    List<T> merged = new ArrayList<T>();
    for (Cachable cachable : cachables) {
      List<Cachable> cached = get(cachable.getCacheKey());
      if (!CollectionUtils.isEmpty(cached)) {
        merged.add(clazz.cast(cached.get(0)));
      } else {
        put(cachable, Action.UPDATE);
        merged.add(clazz.cast(cachable));
      }
    }
    return merged;
  }
  
//  public synchronized <T extends Cachable> void putIfAbsent(List<T> cachables) {
//    if (cachables == null) {
//      return;
//    }
//    for (T cachable : cachables) {
//      List<Cachable> fromDB = get(cachable.getCacheKey());
//      if (fromDB.isEmpty()) {
//        put(cachable, Action.UPDATE);
//      }
//    }
//  }
  
  public synchronized List<Cachable> get(CacheKey search) {
    List<Cachable> result = new ArrayList<>();
    for (Entry<CacheKey, CacheItem> entry : cache.entrySet()) {
      if (entry.getKey().satisfies(search)) {
        result.add(entry.getValue().cachable);
        entry.getValue().hit();
        hits++;
      }
    }
    return result;
  }

}
