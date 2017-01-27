package org.rabix.engine.rest.service.impl;

import org.rabix.engine.db.BackendDB;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.BackendPopulator;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendServiceImpl implements BackendService {

  private final static Logger logger = LoggerFactory.getLogger(BackendServiceImpl.class);
  
  private final BackendDB backendDB;
  private final JobService jobService;
  private final BackendPopulator backendPopulator;
  private final BackendDispatcher backendDispatcher;
  private final BackendStubFactory backendStubFactory;
  
  @Inject
  public BackendServiceImpl(JobService jobService, BackendPopulator backendPopulator, BackendStubFactory backendStubFactory, BackendDB backendDB, BackendDispatcher backendDispatcher) {
    this.backendDB = backendDB;
    this.jobService = jobService;
    this.backendPopulator = backendPopulator;
    this.backendDispatcher = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
  }
  
  @Override
  public <T extends Backend> T create(T backend) throws TransportPluginException {
    backend = backendPopulator.populate(backend);
    backendDB.add(backend);
    
    BackendStub<?, ?, ?> backendStub = backendStubFactory.create(jobService, backend);
    backendDispatcher.addBackendStub(backendStub);
    
    logger.info("Backend {} registered.", backend.getId());
    return backend;
  }
  
}
