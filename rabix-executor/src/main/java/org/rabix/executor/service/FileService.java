package org.rabix.executor.service;

import java.util.Map;

public interface FileService {

  void delete(String rootId, Map<String, Object> config);
  
}
