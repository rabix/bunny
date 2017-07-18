package org.rabix.common.ftp;

import org.apache.commons.configuration.Configuration;

public class FTPConfig {

  public static String getHost(Configuration configuration) {
    return configuration.getString("ftp.host");
  }

  public static int getPort(Configuration configuration) {
    return configuration.getInt("ftp.port", 2221);
  }

  public static String getUsername(Configuration configuration) {
    return configuration.getString("ftp.username");
  }

  public static String getPassword(Configuration configuration) {
    return configuration.getString("ftp.password");
  }
  
  public static boolean isFTPEnabled(Configuration configuration) {
    return configuration.getBoolean("ftp.enabled", false);
  }

  public static String getDirectory(Configuration configuration) {
    return configuration.getString("ftp.directory");
  }

}
