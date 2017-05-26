package org.rabix.storage.memory.impl;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.rabix.storage.model.BackendRecord;
import org.rabix.storage.repository.BackendRepository;

import com.google.inject.Inject;

public class InMemoryBackendRepository implements BackendRepository{

  private Map<UUID, BackendRecord> backendRepository;
  
  @Inject
  public InMemoryBackendRepository() {
    this.backendRepository = new ConcurrentHashMap<>();
  }

  @Override
  public synchronized void insert(BackendRecord backend) {
    backendRepository.put(backend.getId(), backend);
  }

  @Override
  public synchronized BackendRecord get(UUID id) {
    return backendRepository.get(id);
  }

  @Override
  public synchronized List<BackendRecord> getByStatus(BackendRecord.Status status) {
    return backendRepository.values().stream()
        .filter(e -> e.getStatus() == status)
        .collect(Collectors.toList());
  }

  @Override
  public synchronized void updateConfiguration(UUID id, Map<String, ?> backendConfiguration) {
    backendRepository.get(id).setBackendConfig(backendConfiguration);
  }

  @Override
  public synchronized void updateHeartbeatInfo(UUID id, Instant heartbeatInfo) {
    backendRepository.get(id).setHeartbeatInfo(heartbeatInfo);
  }

  @Override
  public synchronized void updateStatus(UUID id, BackendRecord.Status status) {
    backendRepository.get(id).setStatus(status);
  }

  @Override
  public synchronized Instant getHeartbeatInfo(UUID id) {
    return backendRepository.get(id).getHeartbeatInfo();
  }

  @Override
  public List<BackendRecord> getAll() {
    return new ArrayList<>(backendRepository.values());
  }
  
}
