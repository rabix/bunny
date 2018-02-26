package org.rabix.backend.tes.config;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class TESConfig {

  public static final String HOST = "rabix.tes.client_host";
  public static final String PORT = "rabix.tes.client_port";
  public static final String SCHEME = "rabix.tes.client_scheme";
  public static final String CONNECT_TIMEOUT = "rabix.tes.client_connect_timeout";
  public static final String READ_TIMEOUT = "rabix.tes.client_read_timeout";
  public static final String WRITE_TIMEOUT = "rabix.tes.client_write_timeout";

  public static final String TASK_THREAD_POOL = "rabix.tes.task_thread_pool";
  public static final String POSTPROCESSING_THREAD_POOL = "rabix.tes.postprocessing_thread_pool";

  public static final String STORAGE_BASE = "rabix.tes.storage_base";

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
    return configuration.getInt(TASK_THREAD_POOL, 10);
  }

  public int getPostProcessingThreadPoolSize() {
    return configuration.getInt(POSTPROCESSING_THREAD_POOL, 1);
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

  public String getStorageBase() { return configuration.getString(STORAGE_BASE); }

}
