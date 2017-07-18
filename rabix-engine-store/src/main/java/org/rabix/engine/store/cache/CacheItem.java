package org.rabix.engine.store.cache;

public class CacheItem {

  public static enum Action {
    INSERT,
    UPDATE,
    NOOP
  }
  
  public Action action;
  public Cachable cachable;
  public boolean isDirty;
  
  public CacheItem(Action action, Cachable cachable) {
    this.action = action;
    this.cachable = cachable;
  }

  public void reset() {
    action = Action.NOOP;
  }
  
  @Override
  public String toString() {
    return "CacheItem [action=" + action + ", cachable=" + cachable + "]";
  }
  
}
