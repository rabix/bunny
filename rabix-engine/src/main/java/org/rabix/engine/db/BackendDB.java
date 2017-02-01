package org.rabix.engine.db;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.transport.backend.Backend;

import com.google.inject.Inject;

public class BackendDB {

  private BackendRepository backendRepository;
  
  @Inject
  public BackendDB(BackendRepository backendRepository) {
    this.backendRepository = backendRepository;
  }
  
  public void add(Backend backend) {
    backendRepository.insert(backend.getId(), backend);
  }
  
  public void update(Backend backend) {
    backendRepository.update(backend.getId(), backend);
  }
  
  public Backend get(String id) {
    return backendRepository.get(id);
  }
  
}
