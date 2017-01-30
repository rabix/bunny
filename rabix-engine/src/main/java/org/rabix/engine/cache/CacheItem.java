package org.rabix.engine.cache;

public class CacheItem {

  public static enum Action {
    INSERT,
    UPDATE
  }
  
  public Action action;
  public Cachable cachable;
  
  public CacheItem(Action action, Cachable cachable) {
    this.action = action;
    this.cachable = cachable;
  }

  @Override
  public String toString() {
    return "CacheItem [action=" + action + ", cachable=" + cachable + "]";
  }
  
}
