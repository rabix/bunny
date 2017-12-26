package org.rabix.engine.processor.handler.impl;

import com.google.inject.Inject;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.store.model.ContextRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextStatusEventHandler implements EventHandler<ContextStatusEvent> {

  private static final Logger logger = LoggerFactory.getLogger(ContextStatusEventHandler.class);

  private final ContextRecordService contextRecordService;

  @Inject
  public ContextStatusEventHandler(ContextRecordService contextRecordService) {
    this.contextRecordService = contextRecordService;
  }

  @Override
  public void handle(ContextStatusEvent event, EventHandlingMode mode) throws EventHandlerException {
    ContextRecord contextRecord = contextRecordService.find(event.getContextId());
    if (contextRecord == null) {
      logger.warn("Unknown context {}. Skipping...", event.getContextId());
      return;
    }

    contextRecord.setStatus(event.getStatus());
    contextRecordService.update(contextRecord);
  }

}
