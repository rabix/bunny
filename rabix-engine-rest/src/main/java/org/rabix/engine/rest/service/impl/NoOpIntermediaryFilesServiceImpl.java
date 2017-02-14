package org.rabix.engine.rest.service.impl;

import java.util.UUID;

import org.rabix.engine.rest.service.IntermediaryFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpIntermediaryFilesServiceImpl extends IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(NoOpIntermediaryFilesServiceImpl.class);
  
  @Override
  public void handleUnusedFiles(UUID rootId) {
    logger.debug(String.format("handleUnusedFiles(%s)", rootId));
  }

}
