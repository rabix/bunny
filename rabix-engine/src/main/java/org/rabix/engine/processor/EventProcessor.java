package org.rabix.engine.processor;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;

import java.util.UUID;

public interface EventProcessor {

  void start();

  void stop();

  boolean isRunning();

  void send(Event event) throws EventHandlerException;

  void addToQueue(Event event);

  void addToExternalQueue(Event event);

  void addToExternalQueue(Event event, Runnable onProcessed);

  void persist(Event event);

  boolean hasWork();

  void setEventHandlingMode(EventHandler.EventHandlingMode mode);

  boolean isReplayMode();

  int eventsQueueSize();

  class EventProcessorDispatcher {

    public static int dispatch(UUID rootId, int numberOfEventProcessors) {
      return Math.abs(rootId.hashCode() % numberOfEventProcessors);
    }
  }

}
