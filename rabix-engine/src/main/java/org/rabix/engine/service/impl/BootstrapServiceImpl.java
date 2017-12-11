package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.engine.service.GarbageCollectionService;
import org.rabix.engine.store.repository.EventRepository;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class BootstrapServiceImpl implements BootstrapService {

  private final BackendService backendService;
  private final GarbageCollectionService garbageCollectionService;

  private final EventRepository eventRepository;
  private final TransactionHelper transactionHelper;

  private final EventProcessor eventProcessor;
  private final static Logger logger = LoggerFactory.getLogger(BootstrapServiceImpl.class);

  @Inject
  public BootstrapServiceImpl(TransactionHelper transactionHelper, EventRepository eventRepository,
      EventProcessor eventProcessor, BackendService backendService, GarbageCollectionService garbageCollectionService) {
    this.backendService = backendService;
    this.eventProcessor = eventProcessor;
    this.eventRepository = eventRepository;
    this.transactionHelper = transactionHelper;
    this.garbageCollectionService = garbageCollectionService;
  }

  @Override
  public void start() throws BootstrapServiceException {
    try {
      eventProcessor.start();
      backendService.scanEmbedded();
    } catch (Exception e) {
      throw new BootstrapServiceException(e);
    }
  }

  @Override
  public void replay() throws BootstrapServiceException {
    try {
      transactionHelper.doInTransaction(() -> {
        List<Backend> activeBackends = backendService.getActiveRemoteBackends();

        for (Backend backend : activeBackends) {
          logger.debug("Awakening backend: " + backend.getId());
        }
        replayEvents();
        return null;
      });
    } catch (Exception e) {
      throw new BootstrapServiceException(e);
    }
  }

  private void replayEvents() {
    List<Event> events = eventRepository
            .getPendingEvents()
            .stream()
            .map(eventRecord -> JSONHelper.convertToObject(eventRecord.getEvent(), Event.class))
            .collect(Collectors.toList());

    for (Event event : events) {
      eventProcessor.addToExternalQueue(event);
    }
  }
}
