package org.rabix.engine.processor.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.status.EngineStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class MultiEventProcessorImpl implements EventProcessor {

  private final static Logger logger = LoggerFactory.getLogger(MultiEventProcessorImpl.class);
  
  private int eventProcessorCount;
  
  private final ConcurrentMap<Integer, EventProcessorImpl> eventProcessors;

  private volatile boolean isRunning = false;
  
  @Inject
  public MultiEventProcessorImpl(Provider<EventProcessorImpl> singleEventProcessorProvider, Configuration configuration) {
    this.eventProcessorCount = configuration.getInt("bunny.event_processor.count", Runtime.getRuntime().availableProcessors());
    this.eventProcessors = new ConcurrentHashMap<>(eventProcessorCount);
    for (int i = 0; i < eventProcessorCount; i++) {
      this.eventProcessors.put(i, singleEventProcessorProvider.get());
    }
  }

  @Override
  public void start(List<IterationCallback> iterationCallbacks, EngineStatusCallback engineStatusCallback) {
    for (EventProcessorImpl singleEventProcessor : eventProcessors.values()) {
      singleEventProcessor.start(iterationCallbacks, engineStatusCallback);
    }
    this.isRunning = true;
  }

  @Override
  public void stop() {
    for (EventProcessorImpl eventProcessor : eventProcessors.values()) {
      eventProcessor.stop();
    }
    this.isRunning = false;
  }

  @Override
  public void send(Event event) throws EventHandlerException {
    getEventProcessor(event.getContextId()).send(event);
  }

  @Override
  public void addToQueue(Event event) {
    getEventProcessor(event.getContextId()).addToQueue(event);
  }
  
  @Override
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Gets {@link EventProcessor} based on Root ID
   * TODO: discuss load balancing algorithm 
   * 
   * @param rootId  Root ID
   * @return        EventProcessor instance
   */
  private EventProcessor getEventProcessor(String rootId) {
    int index = Math.abs(rootId.hashCode() % eventProcessorCount);
    logger.debug("Root Job {} goes to EventProcessor {}", rootId, index);
    return eventProcessors.get(index);
  }
  
}
