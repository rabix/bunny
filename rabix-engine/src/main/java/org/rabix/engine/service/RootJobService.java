package org.rabix.engine.service;

import org.rabix.engine.model.RootJob;
import org.rabix.engine.repository.RootJobRepository;

import com.google.inject.Inject;

import java.util.UUID;

public class RootJobService {

  private RootJobRepository rootJobRepository;
  
  @Inject
  public RootJobService(RootJobRepository rootJobRepository) {
    this.rootJobRepository = rootJobRepository;
  }
  
  public synchronized void create(RootJob rootJob) {
    rootJobRepository.insert(rootJob);
  }
  
  public synchronized void update(RootJob context) {
    rootJobRepository.update(context);
  }
  
  public synchronized RootJob find(UUID id) {
    return rootJobRepository.get(id);
  }

}
