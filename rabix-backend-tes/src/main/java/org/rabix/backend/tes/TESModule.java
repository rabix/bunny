package org.rabix.backend.tes;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.tes.client.TESHttpClient;
import org.rabix.backend.tes.service.TESStorageService;
import org.rabix.backend.tes.service.impl.LocalTESStorageServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl;
import org.rabix.backend.tes.service.impl.LocalTESWorkerServiceImpl.TESWorker;
import org.rabix.common.config.ConfigModule;

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
    String[] s3EndpointConfig = configuration.getStringArray("s3fs_endpoints");
    ArrayList<String> s3Endpoints = new ArrayList<>();

    if (s3EndpointConfig != null && s3EndpointConfig.length > 0) {
      s3Endpoints.addAll(Arrays.asList(s3EndpointConfig));
    }

    if (storageConfig != null && storageConfig.startsWith("s3")) {
      s3Endpoints.add(storageConfig);
    }

    Map<String, String> env = new HashMap<>();
    env.put(AmazonS3Factory.ACCESS_KEY, configuration.getString(AmazonS3Factory.ACCESS_KEY));
    env.put(AmazonS3Factory.SECRET_KEY, configuration.getString(AmazonS3Factory.SECRET_KEY));

    if (configuration.getString(AmazonS3Factory.CONNECTION_TIMEOUT) != null) {
      env.put(AmazonS3Factory.CONNECTION_TIMEOUT, configuration.getString(AmazonS3Factory.CONNECTION_TIMEOUT));
    }
    if (configuration.getString(AmazonS3Factory.MAX_CONNECTIONS) != null) {
      env.put(AmazonS3Factory.MAX_CONNECTIONS, configuration.getString(AmazonS3Factory.MAX_CONNECTIONS));
    }
    if (configuration.getString(AmazonS3Factory.MAX_ERROR_RETRY) != null) {
      env.put(AmazonS3Factory.MAX_ERROR_RETRY, configuration.getString(AmazonS3Factory.MAX_ERROR_RETRY));
    }
    if (configuration.getString(AmazonS3Factory.PROTOCOL) != null) {
      env.put(AmazonS3Factory.PROTOCOL, configuration.getString(AmazonS3Factory.PROTOCOL));
    }
    if (configuration.getString(AmazonS3Factory.SIGNER_OVERRIDE) != null) {
      env.put(AmazonS3Factory.SIGNER_OVERRIDE, configuration.getString(AmazonS3Factory.SIGNER_OVERRIDE));
    }
    if (configuration.getString(AmazonS3Factory.PATH_STYLE_ACCESS) != null) {
      env.put(AmazonS3Factory.PATH_STYLE_ACCESS, configuration.getString(AmazonS3Factory.PATH_STYLE_ACCESS));
    }

    for (String endpoint : s3Endpoints) {
      try {
        URI uri = URI.create(endpoint);
        FileSystems.newFileSystem(uri, env, Thread.currentThread().getContextClassLoader());
      } catch (java.nio.file.FileSystemAlreadyExistsException e) {
        // do nothing
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
