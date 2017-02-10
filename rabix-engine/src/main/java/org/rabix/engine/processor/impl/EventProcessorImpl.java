package org.rabix.engine.processor.impl;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rabix.bindings.model.Job;
import org.rabix.engine.SchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.EventType;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.engine.service.CacheService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
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
  private final BlockingQueue<Event> externalEvents = new LinkedBlockingQueue<>();
  
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final AtomicBoolean stop = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private final HandlerFactory handlerFactory;
  
  private final ContextRecordService contextRecordService;
  
  private final TransactionHelper transactionHelper;
  private final CacheService cacheService;
  
  private final JobRepository jobRepository;
  private final EventRepository eventRepository;
  
  @Inject
  public EventProcessorImpl(HandlerFactory handlerFactory, ContextRecordService contextRecordService,
      TransactionHelper transactionHelper, CacheService cacheService, EventRepository eventRepository,
      JobRepository jobRepository) {
    this.handlerFactory = handlerFactory;
    this.contextRecordService = contextRecordService;
    this.transactionHelper = transactionHelper;
    this.cacheService = cacheService;
    this.eventRepository = eventRepository;
    this.jobRepository = jobRepository;
  }

  public void start(EngineStatusCallback engineStatusCallback) {
    this.handlerFactory.initialize(engineStatusCallback);
    
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        Event event = null;
        while (!stop.get()) {
          try {
            event = externalEvents.poll();
            if (event == null) {
              running.set(false);
              Thread.sleep(SLEEP);
              continue;
            }
            running.set(true);
            final Event finalEvent = event;
            transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
              @Override
              public Void call() throws TransactionException {
                handle(finalEvent);
                cacheService.flush(finalEvent.getContextId());
                
                Set<Job> readyJobs = jobRepository.getReadyJobsByGroupId(SchemaHelper.toUUID(finalEvent.getEventGroupId()));
                try {
                  engineStatusCallback.onJobsReady(readyJobs);
                } catch (EngineStatusCallbackException e) {
                  logger.error("Failed to call onJobsReady() callback", e);
                  // TODO handle exception
                }
                eventRepository.delete(UUID.fromString(finalEvent.getEventGroupId()));
                return null;
              }
            });
          } catch (Exception e) {
            logger.error("EventProcessor failed to process event {}.", event, e);
            try {
              invalidateContext(event.getContextId());
            } catch (EventHandlerException ehe) {
              logger.error("Failed to invalidate Context {}.", event.getContextId(), ehe);
              stop();
            }
          }
        }
      }
    });
  }
  
  private void handle(Event event) throws TransactionException {
    while (event != null) {
      try {
        ContextRecord context = contextRecordService.find(event.getContextId());
        if (context != null && context.getStatus().equals(ContextStatus.FAILED)) {
          logger.info("Skip event {}. Context {} has been invalidated.", event, context.getId());
          return;
        }
        handlerFactory.get(event.getType()).handle(event);
      } catch (EventHandlerException e) {
        throw new TransactionException(e);
      }
      event = events.poll();
    }
  }
  
  /**
   * Invalidates context 
   */
  private void invalidateContext(String contextId) throws EventHandlerException {
    handlerFactory.get(Event.EventType.CONTEXT_STATUS_UPDATE).handle(new ContextStatusEvent(contextId, ContextStatus.FAILED));
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
    handlerFactory.get(event.getType()).handle(event);
  }

  public void addToQueue(Event event) {
    if (stop.get()) {
      return;
    }
    this.events.add(event);
  }
  
  public void addToExternalQueue(Event event, boolean persist) {
    if (stop.get()) {
      return;
    }
    if (persist) {
      eventRepository.insert(UUID.fromString(event.getEventGroupId()), event.getPersistentType(), event, EventStatus.UNPROCESSED);
    }
    this.externalEvents.add(event);
  }

}
