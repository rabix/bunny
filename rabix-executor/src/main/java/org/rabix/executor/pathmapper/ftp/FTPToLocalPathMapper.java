package org.rabix.executor.pathmapper.ftp;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.executor.config.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FTPToLocalPathMapper implements FilePathMapper {

  private final static Logger logger = LoggerFactory.getLogger(FTPToLocalPathMapper.class);
  
  private final StorageConfiguration storageConfig;
  
  @Inject
  public FTPToLocalPathMapper(StorageConfiguration storageConfig) {
    this.storageConfig = storageConfig;
  }
  
  @Override
  public String map(String path, Map<String, Object> config) throws FileMappingException {
    logger.info("Map FTP path {} to physical path.", path);
    try {
      return new File(storageConfig.getPhysicalExecutionBaseDir(), path).getCanonicalPath();
    } catch (IOException e) {
      throw new FileMappingException(e);
    }
  }

}
