package org.rabix.engine.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.storage.repository.EventRepository;
import org.rabix.storage.repository.TransactionHelper;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BootstrapServiceImpl implements BootstrapService {

  private BackendService backendService;
  
  private EventRepository eventRepository;
  private TransactionHelper transactionHelper;
  
  private EventProcessor eventProcessor;
  private final static Logger logger = LoggerFactory.getLogger(BootstrapServiceImpl.class);
  
  @Inject
  public BootstrapServiceImpl(TransactionHelper transactionHelper, EventRepository eventRepository, EventProcessor eventProcessor, BackendService backendService) {
    this.backendService = backendService;
    this.eventProcessor = eventProcessor;
    this.eventRepository = eventRepository;
    this.transactionHelper = transactionHelper;
  }
  
  public void replay() throws BootstrapServiceException {
    try {
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          List<Backend> activeBackends = backendService.getActiveBackends();
          
          for (Backend backend : activeBackends) {
            backendService.startBackend(backend);
            logger.debug("Awakening backend: " + backend.getId());
          }
          
          List<Event> events = eventRepository.findUnprocessed().stream()
              .map(er -> JSONHelper.convertToObject(er.getEvent(), Event.class))
              .collect(Collectors.toList());
          
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
