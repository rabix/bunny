package org.rabix.engine.cache;

public interface CachableRepository {

  int insert(Cachable record);
  
  int update(Cachable record);
  
}
