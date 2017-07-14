package org.rabix.backend.lsf;

import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.WorkerService;
import org.rabix.common.config.ConfigModule;
import org.rabix.backend.lsf.service.LSFWorkerServiceImpl;

import com.google.inject.Scopes;

public class LSFModule extends BackendModule {

  public LSFModule(ConfigModule configModule) {
    super(configModule);
  }

  @Override
  protected void configure() {
    bind(WorkerService.class).to(LSFWorkerServiceImpl.class).in(Scopes.SINGLETON);
  }

}
