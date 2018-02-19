package org.rabix.backend.tes.config;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class TESConfig {

  public static final String HOST = "rabix.tes.client-host";
  public static final String PORT = "rabix.tes.client-port";
  public static final String SCHEME = "rabix.tes.client-scheme";
  public static final String CONNECT_TIMEOUT = "rabix.tes.client-connect-timeout";
  public static final String READ_TIMEOUT = "rabix.tes.client-read-timeout";
  public static final String WRITE_TIMEOUT = "rabix.tes.client-write-timeout";
  public static final String TASK_THREAD_POOL_SIZE = "rabix.tes.task-thread-pool-size";
  public static final String POLLING_THREAD_POOL_SIZE = "rabix.tes.polling-thread-pool-size";
  public static final String STORAGE_BASE = "rabix.tes.storage-base";

  private final Configuration configuration;

  @Inject
  public TESConfig(final Configuration configuration) {
    this.configuration = configuration;
  }
  
  public String getHost() {
    return configuration.getString(HOST);
  }
  
  public String getScheme() {
    return configuration.getString(SCHEME);
  }
  
  public int getPort() {
    return configuration.getInt(PORT);
  }

  public int getTaskThreadPoolSize() {
    return configuration.getInt(TASK_THREAD_POOL_SIZE, 1);
  }

  public int getPollingThreadPoolSize() {
    return configuration.getInt(POLLING_THREAD_POOL_SIZE, 10);
  }

  public int getClientConnectTimeout() {
    return configuration.getInt(CONNECT_TIMEOUT, 60);
  }

  public int getClientReadTimeout() {
    return configuration.getInt(READ_TIMEOUT, 60);
  }

  public int getClientWriteTimeout() {
    return configuration.getInt(WRITE_TIMEOUT, 60);
  }

  public String getStorageBase() {
    return configuration.getString(STORAGE_BASE);
  }


}
