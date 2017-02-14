package org.rabix.engine.cache;

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
    this.isDirty = true;
  }

  public void hit() {
    isDirty = true;
    if (action == Action.NOOP) {
      action = Action.UPDATE;
    }
  }
  
  public void reset() {
    isDirty = false;
    action = Action.NOOP;
  }
  
  @Override
  public String toString() {
    return "CacheItem [action=" + action + ", cachable=" + cachable + "]";
  }
  
}
