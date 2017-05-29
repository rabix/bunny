package org.rabix.executor.config.impl;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;

public class LocalStorageConfiguration extends DefaultStorageConfiguration {

  private String rootDir;

  public LocalStorageConfiguration(Configuration configuration, String rootDir) {
    super(configuration);
    this.rootDir = rootDir;
  }
  
  public LocalStorageConfiguration(Configuration configuration) {
    super(configuration);
  }

  /**
   * Returns root dir name, ignores rootId param
   */
  @Override
  public File getRootDir(UUID rootId, Map<String, Object> config) {
    File contextDir = new File(getPhysicalExecutionBaseDir(), rootDir);
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    return contextDir;
  }

}
