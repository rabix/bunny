package org.rabix.executor.service.impl;

import java.io.File;
import java.util.Map;

import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FileServiceImpl implements FileService {

  private final static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
  
  private final StorageConfiguration storageConfiguration;
  
  @Inject
  public FileServiceImpl(StorageConfiguration storageConfiguration) {
    this.storageConfiguration = storageConfiguration;
  }
  
  @Override
  public void delete(String rootId, Map<String, Object> config) {
    File rootDir = storageConfiguration.getRootDir(rootId, config);
    if (rootDir.exists()) {
      rootDir.delete();
    }
    
    logger.info("Directory {} successfully deleted.", rootDir.getAbsolutePath());
  }

}
