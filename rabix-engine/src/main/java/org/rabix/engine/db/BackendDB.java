package org.rabix.engine.db;

import java.sql.Timestamp;

import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.BackendRepository.BackendStatus;
import org.rabix.transport.backend.Backend;

import com.google.inject.Inject;

public class BackendDB {

  private BackendRepository backendRepository;
  
  @Inject
  public BackendDB(BackendRepository backendRepository) {
    this.backendRepository = backendRepository;
  }
  
  public void add(Backend backend, Long heartbeatInfo) {
    backendRepository.insert(backend.getId(), backend, new Timestamp(heartbeatInfo), BackendStatus.ACTIVE);
  }
  
  public void update(Backend backend) {
    backendRepository.update(backend.getId(), backend);
  }
  
  public Backend get(String id) {
    return backendRepository.get(id);
  }
  
}
