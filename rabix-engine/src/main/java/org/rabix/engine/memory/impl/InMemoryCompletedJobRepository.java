package org.rabix.engine.memory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.model.Job;
import org.rabix.engine.repository.CompletedJobRepository;

import com.google.inject.Inject;

public class InMemoryCompletedJobRepository implements CompletedJobRepository{

  private Map<UUID, Map<UUID, Job>> jobRepository;
  
  @Inject
  public InMemoryCompletedJobRepository() {
    this.jobRepository = new ConcurrentHashMap<UUID, Map<UUID, Job>>();
  }

  @Override
  public synchronized void insert(Job job) {
    Map<UUID, Job> rootJobs = jobRepository.get(job.getRootId());
    if (rootJobs == null) {
      rootJobs = new HashMap<>();
      jobRepository.put(job.getRootId(), rootJobs);
    }
    rootJobs.put(job.getId(), job);
  }

  @Override
  public Job get(UUID id) {
    for (Entry<UUID, Map<UUID, Job>> entry : jobRepository.entrySet()) {
      for (Job job : entry.getValue().values()) {
        if (id.equals(job.getId())) {
          return job;
        }
      }
    }
    return null;
  }

}
