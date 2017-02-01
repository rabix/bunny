package org.rabix.engine.repository;

import org.rabix.transport.backend.Backend;

import java.util.UUID;

public interface BackendRepository {

  void insert(Backend backend);

  
  void update(Backend configuration);

  Backend get(UUID id);

  Backend getByName(String name);
  
}