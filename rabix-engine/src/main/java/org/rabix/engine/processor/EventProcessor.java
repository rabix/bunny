package org.rabix.engine.processor;

import java.util.List;
import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.status.EngineStatusCallback;

public interface EventProcessor {

  void start(List<IterationCallback> iterationCallbacks, EngineStatusCallback engineStatusCallback);

  void stop();

  boolean isRunning();

  void send(Event event) throws EventHandlerException;

  void addToQueue(Event event);

  /**
   * Post iteration callback
   */
  public static interface IterationCallback {

    /**
     * Call this method to execute something after one iteration
     */
    void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception;

  }

  public static class EventProcessorDispatcher {

    public static int dispatch(UUID rootId, int numberOfEventProcessors) {
      return Math.abs(rootId.hashCode() % numberOfEventProcessors);
    }

  }

}
