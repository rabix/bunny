package org.rabix.engine.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.StoreCleanupService;
import org.rabix.engine.store.repository.EventRepository;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BootstrapServiceImpl implements BootstrapService {

  private final BackendService backendService;
  private final SchedulerService schedulerService;
  private final StoreCleanupService storeCleanupService;
  
  private final EventRepository eventRepository;
  private final TransactionHelper transactionHelper;
  
  private final EventProcessor eventProcessor;
  private final static Logger logger = LoggerFactory.getLogger(BootstrapServiceImpl.class);
  
  @Inject
  public BootstrapServiceImpl(TransactionHelper transactionHelper, EventRepository eventRepository,
      EventProcessor eventProcessor, BackendService backendService, SchedulerService schedulerService, StoreCleanupService storeCleanupService) {
    this.backendService = backendService;
    this.eventProcessor = eventProcessor;
    this.eventRepository = eventRepository;
    this.transactionHelper = transactionHelper;
    this.schedulerService = schedulerService;
    this.storeCleanupService = storeCleanupService;
  }

  @Override
  public void start() throws BootstrapServiceException {
    try {
      eventProcessor.start();
      backendService.scanEmbedded();
      schedulerService.start();
      storeCleanupService.start();
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
          backendService.startBackend(backend);
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
    List<Event> events = eventRepository.findUnprocessed().stream()
        .map(er -> JSONHelper.convertToObject(er.getEvent(), Event.class))
        .collect(Collectors.toList());

    for (Event event : events) {
      eventProcessor.addToExternalQueue(event);
    }
  }
}
