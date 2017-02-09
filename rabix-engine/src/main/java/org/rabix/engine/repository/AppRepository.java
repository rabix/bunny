package org.rabix.engine.repository;

import java.util.UUID;

import org.rabix.bindings.model.Application;

public interface AppRepository {

  void insert(UUID id, Application app);
  
  Application get(UUID id);
  
}
