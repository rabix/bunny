package org.rabix.engine.service.impl;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class NoOpIntermediaryFilesServiceHandler implements IntermediaryFilesHandler {
  private final static Logger logger = LoggerFactory.getLogger(NoOpIntermediaryFilesServiceHandler.class);

  @Inject
  public NoOpIntermediaryFilesServiceHandler() {}

  @Override
  public void handleUnusedFiles(Job job, Set<String> unusedFiles) {
    logger.debug(String.format("handleUnusedFilesIfAny(%s)", job.getRootId()));
  }

}
