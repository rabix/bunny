package org.rabix.storage.cache;

import java.util.List;

public interface CachableRepository {

  int insertCachable(Cachable record);
  
  void insertCachables(List<Cachable> records);
  
  int updateCachable(Cachable record);
  
  void updateCachables(List<Cachable> records);
  
}
