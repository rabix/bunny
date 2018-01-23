package org.rabix.backend.tes;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.backend.tes.service.impl.LocalTESStorageServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl.TESWorker;
import org.rabix.common.config.ConfigModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.upplication.s3fs.AmazonS3Factory;

public class TESModule extends BackendModule {

  public TESModule(ConfigModule configModule) {
    super(configModule);
  }

  @Override
  protected void configure() {
    bind(TESHttpClient.class).in(Scopes.SINGLETON);
    bind(TESStorageService.class).to(LocalTESStorageServiceImpl.class).in(Scopes.SINGLETON);
    bind(WorkerService.class).annotatedWith(TESWorker.class).to(LocalTESWorkerServiceImpl.class).in(Scopes.SINGLETON);
    Configuration configuration = this.configModule.provideConfig();

    String storageConfig = configuration.getString("rabix.tes.storage.base");
    if (storageConfig != null && storageConfig.startsWith("s3")) {
      Map<String, ?> env = ImmutableMap.<String, Object>builder().put(AmazonS3Factory.ACCESS_KEY, configuration.getString(AmazonS3Factory.ACCESS_KEY))
          .put(AmazonS3Factory.SECRET_KEY, configuration.getString(AmazonS3Factory.SECRET_KEY)).build();
      try {
        URI uri = URI.create(storageConfig);
        FileSystems.newFileSystem(uri, env, Thread.currentThread().getContextClassLoader());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
