package org.rabix.backend.tes;

import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.backend.tes.service.impl.LocalTESStorageServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl.TESWorker;
import org.rabix.common.config.ConfigModule;

import com.google.inject.Scopes;

public class TESModule extends BackendModule {

  public TESModule(ConfigModule configModule) {
    super(configModule);
  }

  @Override
  protected void configure() {
    bind(TESHttpClient.class).in(Scopes.SINGLETON);
    bind(TESStorageService.class).to(LocalTESStorageServiceImpl.class).in(Scopes.SINGLETON);
    bind(WorkerService.class).annotatedWith(TESWorker.class).to(LocalTESWorkerServiceImpl.class).in(Scopes.SINGLETON);
  }

}
