package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.BackendRecord;
import org.rabix.engine.store.repository.BackendRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryBackendRepository implements BackendRepository {

  private final Map<UUID, BackendRecord> backendRepository;

  @Inject
  public InMemoryBackendRepository() {
    this.backendRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(BackendRecord backend) {
    backendRepository.put(backend.getId(), backend);
  }

  @Override
  public BackendRecord get(UUID id) {
    return backendRepository.get(id);
  }

  @Override
  public List<BackendRecord> getByStatus(BackendRecord.Status status) {
    return backendRepository.values().stream()
        .filter(e -> e.getStatus() == status)
        .collect(Collectors.toList());
  }

  @Override
  public void updateConfiguration(UUID id, String backendConfiguration) {
    backendRepository.get(id).setBackendConfig(backendConfiguration);
  }

  @Override
  public void updateHeartbeatInfo(UUID id, Instant heartbeatInfo) {
    backendRepository.get(id).setHeartbeatInfo(heartbeatInfo);
  }

  @Override
  public void updateStatus(UUID id, BackendRecord.Status status) {
    backendRepository.get(id).setStatus(status);
  }

  @Override
  public Instant getHeartbeatInfo(UUID id) {
    return backendRepository.get(id).getHeartbeatInfo();
  }

  @Override
  public List<BackendRecord> getAll() {
    return new ArrayList<>(backendRepository.values());
  }

}
