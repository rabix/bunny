package org.rabix.engine.service.impl;

import java.util.List;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.stub.BackendStub;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BootstrapServiceImpl implements BootstrapService {

  private BackendService backendService;
  
  private EventRepository eventRepository;
  private BackendRepository backendRepository;
  private TransactionHelper transactionHelper;
  
  private EventProcessor eventProcessor;
  private SchedulerService scheduler;
  private final static Logger logger = LoggerFactory.getLogger(BootstrapServiceImpl.class);
  
  @Inject
  public BootstrapServiceImpl(SchedulerService scheduler, TransactionHelper transactionHelper, EventRepository eventRepository, EventProcessor eventProcessor, BackendService backendService, BackendRepository backendRepository) {
    this.backendService = backendService;
    this.backendRepository = backendRepository;
    this.eventProcessor = eventProcessor;
    this.eventRepository = eventRepository;
    this.transactionHelper = transactionHelper;
    this.scheduler = scheduler;
  }
  
  public void replay() throws BootstrapServiceException {
    try {
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          List<Backend> activeBackends = backendRepository.getByStatus(BackendStatus.ACTIVE);
          
          for (Backend backend : activeBackends) {
            BackendStub<?, ?, ?> startBackend = backendService.startBackend(backend);
            scheduler.addBackendStub(startBackend);
            logger.debug("Awakening backend: " + backend.getId());
          }
          
          List<Event> events = eventRepository.findUnprocessed();
          
          for(Event event : events) {
            eventProcessor.addToExternalQueue(event);
          }
          return null;
        }
      });
    } catch (Exception e) {
      throw new BootstrapServiceException(e);
    }
  }
}
