package org.rabix.engine.rest.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.BackendRepository.BackendStatus;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.BootstrapService;
import org.rabix.engine.rest.service.BootstrapServiceException;
import org.rabix.transport.backend.Backend;

import com.google.inject.Inject;

public class BootstrapServiceImpl implements BootstrapService {

  private BackendService backendService;
  
  private EventRepository eventRepository;
  private BackendRepository backendRepository;
  private TransactionHelper transactionHelper;
  
  private EventProcessor eventProcessor;
  
  @Inject
  public BootstrapServiceImpl(TransactionHelper transactionHelper, EventRepository eventRepository, EventProcessor eventProcessor, BackendService backendService, BackendRepository backendRepository) {
    this.backendService = backendService;
    this.backendRepository = backendRepository;
    this.eventProcessor = eventProcessor;
    this.eventRepository = eventRepository;
    this.transactionHelper = transactionHelper;
  }
  
  public void replay() throws BootstrapServiceException {
    try {
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          List<Backend> activeBackends = backendRepository.getByStatus(BackendStatus.ACTIVE);
          
          for (Backend backend : activeBackends) {
            backendRepository.updateHeartbeatInfo(backend.getId(), new Timestamp(System.currentTimeMillis()));
            backendService.startBackend(backend);
          }
          
          List<Event> events = eventRepository.findUnprocessed();
          
          for(Event event : events) {
            eventProcessor.addToExternalQueue(event, false);
          }
          return null;
        }
      });
    } catch (Exception e) {
      throw new BootstrapServiceException(e);
    }
  }
}
