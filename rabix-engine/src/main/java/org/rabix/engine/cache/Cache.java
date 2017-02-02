package org.rabix.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.engine.cache.CacheItem.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache {

  private final static Logger logger = LoggerFactory.getLogger(Cache.class);

  private CachableRepository repository;

  private Map<CacheKey, CacheItem> cache = new HashMap<>();

  public Cache(CachableRepository repository) {
    this.repository = repository;
  }

  public void clear() {
    cache.clear();
  }

  public void flush() {
    if (cache.isEmpty()) {
      return;
    }
    Collection<CacheItem> items = cache.values();

    List<CacheItem> inserts = new ArrayList<>();
    List<CacheItem> updates = new ArrayList<>();

    int size = 0;
    for (CacheItem item : items) {
      switch (item.action) {
      case INSERT:
        inserts.add(item);
        size++;
        break;
      case UPDATE:
        updates.add(item);
        size++;
        break;
      default:
        break;
      }
    }

    for (CacheItem item : inserts) {
      repository.insert(item.cachable);
    }

    for (CacheItem item : updates) {
      repository.update(item.cachable);
    }
    cache.clear();
    logger.debug("Flushed " + size + " item(s).");
  }

  public void put(Cachable cachable, Action action) {
    CacheKey key = cachable.getCacheKey();
    if (cache.containsKey(key)) {
      CacheItem item = cache.get(key);
      item.cachable = cachable;
    } else {
      cache.put(key, new CacheItem(action, cachable));
    }
  }

  public <T extends Cachable> List<T> merge(List<T> cachables, Class<T> clazz) {
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
  
  
  public List<Cachable> get(CacheKey search) {
    List<Cachable> result = new ArrayList<>();
    for (Entry<CacheKey, CacheItem> entry : cache.entrySet()) {
      if (entry.getKey().satisfies(search)) {
        result.add(entry.getValue().cachable);
      }
    }
    return result;
  }

  public boolean isEmpty() {
    return cache.isEmpty();
  }

}
