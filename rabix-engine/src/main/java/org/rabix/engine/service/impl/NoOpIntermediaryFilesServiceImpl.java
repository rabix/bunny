package org.rabix.engine.service.impl;

import org.rabix.bindings.model.Job;
import org.rabix.engine.service.IntermediaryFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpIntermediaryFilesServiceImpl extends IntermediaryFilesService {

  private final static Logger logger = LoggerFactory.getLogger(NoOpIntermediaryFilesServiceImpl.class);
  
  @Override
  public void handleUnusedFiles(Job job) {
    logger.debug(String.format("handleUnusedFiles(%s)", job.getRootId()));
  }

}
