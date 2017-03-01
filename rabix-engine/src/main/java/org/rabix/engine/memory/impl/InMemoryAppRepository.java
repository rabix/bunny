package org.rabix.engine.memory.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.engine.repository.AppRepository;

import com.google.inject.Inject;

public class InMemoryAppRepository implements AppRepository{

  private Map<String, String> appRepository ;
  
  @Inject
  public InMemoryAppRepository() {
    this.appRepository = new ConcurrentHashMap<String, String>();
  }

  @Override
  public synchronized void insert(String hash, String app) {
    appRepository.put(hash, app);
  }

  @Override
  public synchronized String get(String hash) {
    return appRepository.get(hash);
  }

}
