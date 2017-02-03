package org.rabix.engine.repository;

import org.rabix.bindings.model.Application;

import java.util.UUID;

public interface AppRepository {

  void insert(UUID id, Application app);
  
  Application get(UUID id);
  
}
