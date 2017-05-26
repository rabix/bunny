package org.rabix.backend.api;

import org.rabix.common.config.ConfigModule;

import com.google.inject.AbstractModule;

public abstract class BackendModule extends AbstractModule {

  protected ConfigModule configModule;
  
  public BackendModule(ConfigModule configModule) {
    this.configModule = configModule;
  }
  
}
