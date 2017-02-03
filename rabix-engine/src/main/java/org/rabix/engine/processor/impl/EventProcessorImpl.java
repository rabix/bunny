package org.rabix.engine.processor.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventType;
import org.rabix.engine.event.impl.RootJobStatusEvent;
import org.rabix.engine.model.RootJob;
import org.rabix.engine.model.RootJob.RootJobStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.dispatcher.EventDispatcher;
import org.rabix.engine.processor.dispatcher.EventDispatcherFactory;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.engine.service.RootJobService;
import org.rabix.engine.status.EngineStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Event processor implementation
 */
public class EventProcessorImpl implements EventProcessor {

  private static final Logger logger = LoggerFactory.getLogger(EventProcessorImpl.class);
  
  public final static long SLEEP = 100;
  
  private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final AtomicBoolean stop = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private final HandlerFactory handlerFactory;
  private final EventDispatcher eventDispatcher;
  
  private final RootJobService rootJobService;
  
  private final TransactionHelper transactionHelper;
  
  private final ConcurrentMap<UUID, Integer> iterations = new ConcurrentHashMap<>();
  
  @Inject
  public EventProcessorImpl(HandlerFactory handlerFactory, EventDispatcherFactory eventDispatcherFactory, RootJobService rootJobService, TransactionHelper transactionHelper) {
    this.handlerFactory = handlerFactory;
    this.rootJobService = rootJobService;
    this.transactionHelper = transactionHelper;
    this.eventDispatcher = eventDispatcherFactory.create(EventDispatcher.Type.SYNC);
  }

  public void start(final List<IterationCallback> iterationCallbacks, EngineStatusCallback engineStatusCallback) {
    this.handlerFactory.initialize(engineStatusCallback);
    
    final AtomicBoolean shouldSkipIteration = new AtomicBoolean(false);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        Event event = null;
        while (!stop.get()) {
          try {
            event = events.poll();
            if (event == null) {
              running.set(false);
              Thread.sleep(SLEEP);
              continue;
            }
            final Event finalEvent = event;
            transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
              @Override
              public Void call() throws TransactionException {
                RootJob context = rootJobService.find(finalEvent.getRootId());
                if (context != null && context.getStatus().equals(RootJobStatus.FAILED)) {
                  logger.info("Skip event {}. Context {} has been invalidated.", finalEvent, context.getId());
                  shouldSkipIteration.set(true);
                  return null;
                }
                running.set(true);
                try {
                  handlerFactory.get(finalEvent.getType()).handle(finalEvent);
                } catch (EventHandlerException e) {
                  throw new TransactionException(e);
                }
                return null;
              }
            });
            
            if (shouldSkipIteration.get()) {
              shouldSkipIteration.set(false);
              continue;
            }

            Integer iteration = iterations.get(event.getRootId());
            if (iteration == null) {
              iteration = 0;
            }
            
            iteration++;
            if (iterationCallbacks != null) {
              for (IterationCallback callback : iterationCallbacks) {
                callback.call(EventProcessorImpl.this, event.getRootId(), iteration);
              }
            }
            iterations.put(event.getRootId(), iteration);
          } catch (Exception e) {
            logger.error("EventProcessor failed to process event {}.", event, e);
            try {
              invalidateContext(event.getRootId());
            } catch (EventHandlerException ehe) {
              logger.error("Failed to invalidate Context {}.", event.getRootId(), ehe);
              stop();
            }
          }
        }
      }
    });
  }
  
  /**
   * Invalidates context 
   */
  private void invalidateContext(UUID rootId) throws EventHandlerException {
    handlerFactory.get(Event.EventType.CONTEXT_STATUS_UPDATE).handle(new RootJobStatusEvent(rootId, RootJobStatus.FAILED));
  }
  
  @Override
  public void stop() {
    stop.set(true);
    running.set(false);
  }

  public boolean isRunning() {
    return running.get();
  }

  public void send(Event event) throws EventHandlerException {
    if (stop.get()) {
      return;
    }
    if (event.getType().equals(EventType.INIT)) {
      addToQueue(event);
      return;
    }
    eventDispatcher.send(event);
  }

  public void addToQueue(Event event) {
    if (stop.get()) {
      return;
    }
    this.events.add(event);
  }

}
