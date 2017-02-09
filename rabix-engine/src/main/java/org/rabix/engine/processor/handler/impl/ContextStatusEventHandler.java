package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;

import com.google.inject.Inject;

public class ContextStatusEventHandler implements EventHandler<ContextStatusEvent> {

  private final ContextRecordService contextRecordService;

  @Inject
  public ContextStatusEventHandler(ContextRecordService contextRecordService) {
    this.contextRecordService = contextRecordService;
  }
  
  @Override
  public void handle(ContextStatusEvent event) throws EventHandlerException {
    ContextRecord contextRecord = contextRecordService.find(event.getContextId());
    contextRecord.setStatus(event.getStatus());
    contextRecordService.update(contextRecord);
  }

}
