package org.rabix.engine.processor.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventType;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.metrics.MetricsHelper;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler.EventHandlingMode;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.service.GarbageCollectionService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.store.model.ContextRecord.ContextStatus;
import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.model.JobRecord.JobState;
import org.rabix.engine.store.repository.EventRepository;
import org.rabix.engine.store.repository.JobRepository;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.store.repository.TransactionHelper.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Event processor implementation
 */
public class EventProcessorImpl implements EventProcessor {

  private static final Logger logger = LoggerFactory.getLogger(EventProcessorImpl.class);

  private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();
  private final BlockingQueue<ExternalEvent> externalEvents = new LinkedBlockingQueue<>();

  private final ExecutorService executorService =
          Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "EventProcessorThread" + r.hashCode()));

  private final AtomicBoolean stop = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicReference<EventHandlingMode> mode = new AtomicReference<>();

  private final HandlerFactory handlerFactory;

  private final TransactionHelper transactionHelper;
  private final JobRepository jobRepository;
  private final EventRepository eventRepository;
  private final JobService jobService;
  private final MetricsHelper metricsHelper;
  private final GarbageCollectionService garbageCollectionService;

  @Inject
  public EventProcessorImpl(HandlerFactory handlerFactory,
                            TransactionHelper transactionHelper,
                            EventRepository eventRepository,
                            JobRepository jobRepository,
                            JobService jobService,
                            MetricsHelper metricsHelper,
                            GarbageCollectionService garbageCollectionService) {
    this.handlerFactory = handlerFactory;
    this.transactionHelper = transactionHelper;
    this.eventRepository = eventRepository;
    this.jobRepository = jobRepository;
    this.jobService = jobService;
    this.metricsHelper = metricsHelper;
    this.garbageCollectionService = garbageCollectionService;
  }

  public void start() {
    executorService.execute(() -> {
      while (!stop.get()) {
        try {
          ExternalEvent externalEvent = externalEvents.take();
          running.set(true);

          metricsHelper.time(() -> doProcessEvent(externalEvent), "EventProcessorImpl.processEvent");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  private void doProcessEvent(final ExternalEvent externalEvent) {
    final Event event = externalEvent.event;

    try {
      transactionHelper.doInTransaction((TransactionHelper.TransactionCallback<Void>) () -> {
        if (!handle(event, mode.get())) {
          eventRepository.deleteGroup(event.getEventGroupId());
          return null;
        }

        processReadyJobs(event);
        if (event.getType() != EventType.INIT) {
          persist(event);
        }
        return null;
      });
    } catch (Exception e) {
      logger.error("EventProcessor failed to process event {}.", event, e);
      try {
        Job job = jobRepository.get(event.getContextId());
        job = Job.cloneWithMessage(job, "EventProcessor failed to process event:\n" + event.toString());
        jobRepository.update(job);
        jobService.handleJobRootFailed(job);
      } catch (Exception ex) {
        logger.error("Failed to call jobFailed handler for job after event {} failed.", e, ex);
      }

      try {
        EventRecord er = new EventRecord(event.getContextId(), event.getEventGroupId(), EventRecord.Status.FAILED, JSONHelper.convertToMap(e));
        eventRepository.insert(er);
        invalidateContext(event.getContextId());
      } catch (Exception ehe) {
        logger.error("Failed to invalidate Context {}.", event.getContextId(), ehe);
      }
    } finally {
      if (externalEvent.callback != null) {
        externalEvent.callback.run();
      }

      if (mode.get() != EventHandlingMode.REPLAY && event.getType() != EventType.INIT) {
        garbageCollectionService.gc(event.getContextId());
      }
    }
  }

  private void processReadyJobs(Event event) {
    if (!shouldProcessReadyJobs(event) || isReplayMode()) {
      return;
    }

    Set<Job> readyJobs = jobRepository.getReadyJobsByGroupId(event.getEventGroupId());
    jobService.handleJobsReady(readyJobs, event.getContextId(), event.getProducedByNode());
  }

  private boolean shouldProcessReadyJobs(Event event) {
    return (event.getType() == EventType.INIT || (event.getType() == EventType.JOB_STATUS_UPDATE && ((JobStatusEvent) event).getState().equals(JobState.COMPLETED)));
  }

  private boolean handle(Event event, EventHandlingMode mode) throws TransactionException {
    try {
      while (event != null) {
        handlerFactory.get(event.getType()).handle(event, mode);
        event = events.poll();
      }
    } catch (EventHandlerException e) {
      throw new TransactionException(e);
    }

    return true;
  }

  /**
   * Invalidates context
   */
  private void invalidateContext(UUID contextId) throws EventHandlerException {
    handlerFactory.get(Event.EventType.CONTEXT_STATUS_UPDATE).handle(new ContextStatusEvent(contextId, ContextStatus.FAILED), EventHandlingMode.NORMAL);
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
    handlerFactory.get(event.getType()).handle(event, mode.get());
  }

  public void addToQueue(Event event) {
    if (stop.get()) {
      return;
    }
    this.events.add(event);
  }

  @Override
  public void addToExternalQueue(Event event) {
    addToExternalQueue(event, null);
  }

  @Override
  public void addToExternalQueue(Event event, Runnable onProcessed) {
    if (stop.get()) {
      return;
    }
    this.externalEvents.add(new ExternalEvent(event, onProcessed));
  }

  @Override
  public void persist(Event event) {
    if (stop.get() || mode.get() == EventHandlingMode.REPLAY) {
      return;
    }

    logger.debug("persist(event={})", event.toString());

    EventRecord er = new EventRecord(event.getContextId(), event.getEventGroupId(), EventRecord.Status.UNPROCESSED, JSONHelper.convertToMap(event));
    eventRepository.insert(er);
  }

  @Override
  public boolean hasWork() {
    return !externalEvents.isEmpty() || !events.isEmpty();
  }

  @Override
  public void setEventHandlingMode(EventHandlingMode mode) {
    this.mode.set(mode);
  }

  @Override
  public boolean isReplayMode() {
    return this.mode.get() == EventHandlingMode.REPLAY;
  }

  @Override
  public int eventsQueueSize() {
    return events.size();
  }

  /**
   * A callback of an event is called when the event is completely processed
   */
  private static class ExternalEvent {

    private final Runnable callback;
    private final Event event;

    private ExternalEvent(Event event, Runnable callback) {
      this.event = event;
      this.callback = callback;
    }
  }
}
