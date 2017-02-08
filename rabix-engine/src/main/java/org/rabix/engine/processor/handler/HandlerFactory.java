package org.rabix.engine.processor.handler;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventType;
import org.rabix.engine.processor.handler.impl.RootJobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.status.EngineStatusCallback;

import com.google.inject.Inject;

public class HandlerFactory {

  private final InitEventHandler initEventHandler;
  private final InputEventHandler inputEventHandler;
  private final OutputEventHandler outputEventHandler;
  private final JobStatusEventHandler statusEventHandler;
  private final RootJobStatusEventHandler rootJobStatusEventHandler;
  
  @Inject
  public HandlerFactory(InitEventHandler initEventHandler, InputEventHandler inputEventHandler, OutputEventHandler outputEventHandler, JobStatusEventHandler statusEventHandler, RootJobStatusEventHandler rootJobStatusEventHandler) {
    this.initEventHandler = initEventHandler;
    this.inputEventHandler = inputEventHandler;
    this.outputEventHandler = outputEventHandler;
    this.statusEventHandler = statusEventHandler;
    this.rootJobStatusEventHandler = rootJobStatusEventHandler;
  }
  
  /**
   * Initialize some callbacks 
   */
  public void initialize(EngineStatusCallback engineStatusCallback) {
    this.statusEventHandler.initialize(engineStatusCallback);
    this.outputEventHandler.initialize(engineStatusCallback);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Event> EventHandler<T> get(EventType eventType) {
    switch (eventType) {
    case INIT:
      return (EventHandler<T>) initEventHandler;
    case INPUT_UPDATE:
      return (EventHandler<T>) inputEventHandler;
    case OUTPUT_UPDATE:
      return (EventHandler<T>) outputEventHandler;
    case JOB_STATUS_UPDATE:
      return (EventHandler<T>) statusEventHandler;
    case CONTEXT_STATUS_UPDATE:
      return (EventHandler<T>) rootJobStatusEventHandler;
    default:
      throw new RuntimeException("There's no EventHandler for event type " + eventType);
    }
  }
  
}
