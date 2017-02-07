package org.rabix.engine.processor;

import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.status.EngineStatusCallback;

public interface EventProcessor {

  void start(EngineStatusCallback engineStatusCallback);

  void stop();

  boolean isRunning();

  void send(Event event) throws EventHandlerException;

  void addToQueue(Event event);

  void addToExternalQueue(Event event, boolean persist);

  public static class EventProcessorDispatcher {

    public static int dispatch(UUID rootId, int numberOfEventProcessors) {
      return Math.abs(rootId.hashCode() % numberOfEventProcessors);
    }

  }

}
