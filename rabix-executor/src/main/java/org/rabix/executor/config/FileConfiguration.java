package org.rabix.executor.config;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

import com.google.inject.Inject;

public class FileConfiguration {
  
  public static final String CALCULATE_FILE_CHECKSUM = "rabix.calculate_file_checksum";
  public static final String CHECKSUM_ALGORITHM = "rabix.checksum_algorithm";

  private final Configuration configuration;

  @Inject
  public FileConfiguration(final Configuration configuration) {
    this.configuration = configuration;
  }
  
  public boolean calculateFileChecksum() {
    return configuration.getBoolean(CALCULATE_FILE_CHECKSUM, true);
  }

  public HashAlgorithm checksumAlgorithm() {
    return HashAlgorithm.valueOf(configuration.getString(CHECKSUM_ALGORITHM, "SHA1"));
  }
  
}
