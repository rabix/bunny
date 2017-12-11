package org.rabix.engine.processor.handler;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventType;
import org.rabix.engine.processor.handler.impl.*;

import com.google.inject.Inject;

public class HandlerFactory {

  private final InitEventHandler initEventHandler;
  private final InputEventHandler inputEventHandler;
  private final OutputEventHandler outputEventHandler;
  private final JobStatusEventHandler statusEventHandler;
  private final ContextStatusEventHandler contextStatusEventHandler;
  private final ScatterJobEventHandler scatterJobEventHandler;

  @Inject
  public HandlerFactory(InitEventHandler initEventHandler,
                        InputEventHandler inputEventHandler,
                        OutputEventHandler outputEventHandler,
                        JobStatusEventHandler statusEventHandler,
                        ContextStatusEventHandler contextStatusEventHandler,
                        ScatterJobEventHandler scatterJobEventHandler) {
    this.initEventHandler = initEventHandler;
    this.inputEventHandler = inputEventHandler;
    this.outputEventHandler = outputEventHandler;
    this.statusEventHandler = statusEventHandler;
    this.contextStatusEventHandler = contextStatusEventHandler;
    this.scatterJobEventHandler = scatterJobEventHandler;
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
      return (EventHandler<T>) contextStatusEventHandler;
      case CREATE_JOB_RECORDS:
        return (EventHandler<T>) scatterJobEventHandler;
    default:
      throw new RuntimeException("There's no EventHandler for event type " + eventType);
    }
  }

}
