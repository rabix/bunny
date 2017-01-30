package org.rabix.engine.rest.service.impl;

import org.rabix.engine.db.BackendDB;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
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
  private final TransactionHelper transactionHelper;
  
  @Inject
  public BackendServiceImpl(JobService jobService, BackendPopulator backendPopulator, BackendStubFactory backendStubFactory, BackendDB backendDB, BackendDispatcher backendDispatcher, TransactionHelper transactionHelper) {
    this.backendDB = backendDB;
    this.jobService = jobService;
    this.backendPopulator = backendPopulator;
    this.backendDispatcher = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
    this.transactionHelper = transactionHelper;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Backend> T create(T backend) throws TransactionException {
    return (T) transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Backend>() {
      @Override
      public Backend call() throws TransactionException {
        Backend populated = backendPopulator.populate(backend);
        backendDB.add(populated);

        BackendStub<?, ?, ?> backendStub;
        try {
          backendStub = backendStubFactory.create(jobService, populated);
        } catch (TransportPluginException e) {
          throw new TransactionException(e);
        }
        backendDispatcher.addBackendStub(backendStub);
        logger.info("Backend {} registered.", populated.getId());
        return backend;
      }
    });
  }
  
}
