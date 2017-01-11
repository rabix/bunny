package org.rabix.executor.config.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.configuration.Configuration;


public class LocalStorageConfiguration extends DefaultStorageConfiguration {

  private String rootDir;

  public LocalStorageConfiguration(Configuration configuration) {
    super(configuration);
  }

  public LocalStorageConfiguration(String appPath, Configuration configuration) {
    super(configuration);
    this.rootDir = generateRootId(appPath);
  }

  /**
   * Returns root dir name, ignores rootId param
   */
  @Override
  public File getRootDir(String rootId, Map<String, Object> config) {
    File contextDir = new File(getPhysicalExecutionBaseDir(), rootDir);
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    return contextDir;
  }

  /**
   * Returns a directory name containing the current date and app name
   */
  public static String generateRootId(String path) {
    String name;
    if (path.contains("/")) {
      name = path.substring(path.lastIndexOf('/'));
    } else {
      name = path;
    }
    if (name.contains(".")) {
      name = name.substring(0, name.indexOf('.'));
    }
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return name + df.format(new Date());
  }

}
