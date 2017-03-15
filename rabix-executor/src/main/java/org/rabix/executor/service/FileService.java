package org.rabix.executor.service;

import java.util.Map;
import java.util.UUID;

public interface FileService {

  void delete(UUID rootId, Map<String, Object> config);
  
}
