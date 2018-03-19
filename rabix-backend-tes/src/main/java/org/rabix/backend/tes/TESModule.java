package org.rabix.backend.tes;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.*;

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

    List<S3Config> s3Providers = parseS3Config(configuration);
    for (S3Config s3e : s3Providers) {
      Map<String, Object> env = new HashMap<>();

      if (s3e.AccessKey != null) {
        env.put(AmazonS3Factory.ACCESS_KEY, s3e.AccessKey);
      }
      if (s3e.SecretKey != null) {
        env.put(AmazonS3Factory.SECRET_KEY, s3e.SecretKey);
      }
      if (s3e.Protocol != null) {
        env.put(AmazonS3Factory.PROTOCOL, s3e.Protocol);
      }
      if (s3e.MaxRetryError != null) {
        env.put(AmazonS3Factory.MAX_ERROR_RETRY, s3e.MaxRetryError);
      }
      if (s3e.ConnectionTimeout != null) {
        env.put(AmazonS3Factory.CONNECTION_TIMEOUT, s3e.ConnectionTimeout);
      }
      if (s3e.SocketTimeout != null) {
        env.put(AmazonS3Factory.SOCKET_TIMEOUT, s3e.SocketTimeout);
      }
      if (s3e.MaxConnections != null) {
        env.put(AmazonS3Factory.MAX_CONNECTIONS, s3e.MaxConnections);
      }
      if (s3e.SignerOverride != null) {
        env.put(AmazonS3Factory.SIGNER_OVERRIDE, s3e.SignerOverride);
      }
      env.put(AmazonS3Factory.PATH_STYLE_ACCESS, s3e.PathStyleAccess);

      for (String endpoint : s3e.Endpoints) {
        try {
          URI uri = URI.create(endpoint);
          if (uri.getScheme() == "") {
            throw new java.nio.file.ProviderMismatchException("endpoint "+endpoint+" is missing s3:// scheme");
          }
          FileSystems.newFileSystem(uri, env, Thread.currentThread().getContextClassLoader());
        } catch (java.nio.file.FileSystemAlreadyExistsException e) {
          // do nothing
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public List<S3Config> parseS3Config(Configuration configuration) {
    Map<String, S3Config> s3Providers = new HashMap<>();

    Iterator<String> keys = configuration.getKeys("s3");
    while(keys.hasNext()) {
      String k = keys.next();
      String parts[] = k.split("\\.");
      String fsName = parts[1];
      String prop = parts[2];

      S3Config s3;
      if (s3Providers.containsKey(fsName)) {
        s3 = s3Providers.get(fsName);
      } else {
        s3 = new S3Config();
        s3.Name = fsName;
      }

      switch (prop) {
        case "endpoints":
          s3.Endpoints = configuration.getStringArray(k);
          break;
        case "protocol":
          s3.Protocol = configuration.getString(k);
          break;
        case "max_retry_error":
          s3.MaxRetryError = configuration.getInt(k);
          break;
        case "connection_timeout":
          s3.ConnectionTimeout = configuration.getInt(k);
          break;
        case "max_connections":
          s3.MaxConnections = configuration.getInt(k);
          break;
        case "socket_timeout":
          s3.SocketTimeout = configuration.getInt(k);
          break;
        case "signer_override":
          s3.SignerOverride = configuration.getString(k);
          break;
        case "path_style_access":
          s3.PathStyleAccess = configuration.getBoolean(k);
          break;
        case "access_key":
          s3.AccessKey = configuration.getString(k);
          break;
        case "secret_key":
          s3.SecretKey = configuration.getString(k);
          break;
      }
      s3Providers.put(fsName, s3);
    }
    return new ArrayList<>(s3Providers.values());
  }
}

class S3Config {
  public String   Name;
  public String[] Endpoints;
  public String   AccessKey;
  public String   SecretKey;
  public String   Protocol;
  public Integer  MaxRetryError;
  public Integer  ConnectionTimeout;
  public Integer  MaxConnections;
  public Integer  SocketTimeout;
  public String   SignerOverride;
  public boolean  PathStyleAccess;
}