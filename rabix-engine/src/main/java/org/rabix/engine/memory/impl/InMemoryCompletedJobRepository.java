package org.rabix.engine.memory.impl;

import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.repository.CompletedJobRepository;

public class InMemoryCompletedJobRepository implements CompletedJobRepository{

  @Override
  public void insert(Job job) {
    // DO NOTHING
  }

  @Override
  public boolean exists(UUID id) {
    // DO NOTHING
    return false;
  }

}
