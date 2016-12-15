package org.rabix.backend.local.tes.config;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class TESConfig {

  public static final String HOST = "rabix.tes.client-host";
  public static final String PORT = "rabix.tes.client-port";
  public static final String SCHEME = "rabix.tes.client-scheme";
  
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
  
}
