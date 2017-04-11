package org.rabix.executor.config;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class DockerConfigation {

  private final Configuration configuration;

  @Inject
  public DockerConfigation(final Configuration configuration) {
    this.configuration = configuration;
  }
  
  public boolean isDockerConfigAuthEnabled() {
    return configuration.getBoolean("docker.override.auth.enabled", false);
  }
  
  public boolean isDockerSupported() {
    return configuration.getBoolean("docker.enabled", false);
  }
  
  public boolean removeContainers() {
    return configuration.getBoolean("docker.remove_containers", false);
  }
  
}
