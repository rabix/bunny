package org.rabix.engine.cache;

public class CacheItem {

  public static enum Action {
    INSERT,
    UPDATE,
    NOOP
  }
  
  public Action action;
  public Cachable cachable;
  public boolean isHit;
  
  public CacheItem(Action action, Cachable cachable) {
    this.action = action;
    this.cachable = cachable;
    this.isHit = true;
  }
  
  public void hit() {
    isHit = true;
    if (action == Action.NOOP) {
      action = Action.UPDATE;
    }
  }

  @Override
  public String toString() {
    return "CacheItem [action=" + action + ", cachable=" + cachable + "]";
  }
  
}
