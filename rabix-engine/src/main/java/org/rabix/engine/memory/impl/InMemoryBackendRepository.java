package org.rabix.engine.memory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.transport.backend.Backend;

import com.google.inject.Inject;

public class InMemoryBackendRepository implements BackendRepository{

  Map<UUID, BackendEntity> backendRepository;
  
  @Inject
  public InMemoryBackendRepository() {
    this.backendRepository = new ConcurrentHashMap<UUID, BackendEntity>();
  }

  @Override
  public synchronized void insert(UUID id, Backend backend, Timestamp heartbeatInfo, BackendStatus status) {
    backendRepository.put(id, new BackendEntity(backend, heartbeatInfo, status));
  }

  @Override
  public synchronized void update(UUID id, Backend configuration) {
    backendRepository.get(id).setBackend(configuration);
  }

  @Override
  public synchronized Backend get(UUID id) {
    return backendRepository.get(id).getBackend();
  }

  @Override
  public synchronized List<Backend> getByStatus(BackendStatus status) {
    List<Backend> backends = new ArrayList<Backend>();
    for(BackendEntity backend: backendRepository.values()) {
      if(backend.getBackendStatus().equals(status)) {
        backends.add(backend.getBackend());
      }
    }
    return backends;
  }

  @Override
  public synchronized void updateHeartbeatInfo(UUID id, Timestamp heartbeatInfo) {
    backendRepository.get(id).setHeartbeatInfo(heartbeatInfo);
  }

  @Override
  public synchronized void updateStatus(UUID id, BackendStatus status) {
    backendRepository.get(id).setBackendStatus(status);
  }

  @Override
  public synchronized Timestamp getHeartbeatInfo(UUID id) {
    return backendRepository.get(id).getHeartbeatInfo();
  }
  
}
