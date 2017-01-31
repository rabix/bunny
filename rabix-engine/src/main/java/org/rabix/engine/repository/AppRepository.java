package org.rabix.engine.repository;

import org.rabix.bindings.model.Application;

public interface AppRepository {

  void insert(String id, Application app);
  
  Application get(String id);
  
}
