package org.rabix.engine.repository;

import org.rabix.engine.model.RootJob;

import java.util.UUID;

public interface RootJobRepository {

  int insert(RootJob rootJob);
  
  int update(RootJob rootJob);
  
  RootJob get(UUID id);
  
}
