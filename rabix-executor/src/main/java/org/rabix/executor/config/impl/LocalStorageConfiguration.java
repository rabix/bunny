package org.rabix.executor.config.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FilenameUtils;

public class LocalStorageConfiguration extends DefaultStorageConfiguration {

  private String rootDir;

  public LocalStorageConfiguration(Configuration configuration) {
    super(configuration);
  }

  public LocalStorageConfiguration(String appPath, Configuration configuration) {
    super(configuration);
    this.rootDir = generateDirectoryName(appPath);
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

  /**
   * Returns a directory name containing the current date and app name
   */
  public static String generateDirectoryName(String path) {
    String name = FilenameUtils.getBaseName(path);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmmss.S");
    return name + "-" + df.format(new Date());
  }

}
