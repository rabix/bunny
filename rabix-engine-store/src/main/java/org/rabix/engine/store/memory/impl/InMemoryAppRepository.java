package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.repository.AppRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAppRepository implements AppRepository{

  private final Map<String, String> appRepository ;

  @Inject
  public InMemoryAppRepository() {
    this.appRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(String hash, String app) {
    appRepository.put(hash, app);
  }

  @Override
  public String get(String hash) {
    return appRepository.get(hash);
  }

}
