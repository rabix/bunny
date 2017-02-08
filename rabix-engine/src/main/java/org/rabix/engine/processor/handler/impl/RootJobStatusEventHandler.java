package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.RootJobStatusEvent;
import org.rabix.engine.model.RootJob;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.RootJobService;

import com.google.inject.Inject;

public class RootJobStatusEventHandler implements EventHandler<RootJobStatusEvent> {

  private final RootJobService rootJobService;

  @Inject
  public RootJobStatusEventHandler(RootJobService rootJobService) {
    this.rootJobService = rootJobService;
  }
  
  @Override
  public void handle(RootJobStatusEvent event) throws EventHandlerException {
    RootJob rootJob = rootJobService.find(event.getRootId());
    rootJob.setStatus(event.getStatus());
    rootJobService.update(rootJob);
  }

}
