package org.rabix.engine.repository;

import org.rabix.transport.backend.Backend;

public interface BackendRepository {

  void insert(String id, String backend);
  
  void update(String id, String configuration);
  
  Backend get(String id);
  
}