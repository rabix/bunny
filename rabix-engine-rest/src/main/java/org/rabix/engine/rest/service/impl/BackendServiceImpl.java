package org.rabix.engine.rest.service.impl;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.db.BackendDB;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.BackendServiceException;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.SchedulerService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendServiceImpl implements BackendService {

  private final static Logger logger = LoggerFactory.getLogger(BackendServiceImpl.class);
  
  private final BackendDB backendDB;
  private final JobService jobService;
  private final SchedulerService scheduler;
  private final BackendStubFactory backendStubFactory;
  private final TransactionHelper transactionHelper;
  private final Configuration configuration;
  
  private final BackendRepository backendRepository;

  @Inject
  public BackendServiceImpl(JobService jobService, BackendStubFactory backendStubFactory, BackendDB backendDB,
      SchedulerService backendDispatcher, TransactionHelper transactionHelper, BackendRepository backendRepository,
      Configuration configuration) {
    this.backendDB = backendDB;
    this.jobService = jobService;
    this.scheduler = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
    this.transactionHelper = transactionHelper;
    this.configuration = configuration;
    this.backendRepository = backendRepository;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Backend> T create(T backend) throws TransactionException {
    return (T) transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Backend>() {
      @Override
      public Backend call() throws TransactionException {
        Backend populated = populate(backend);
        backendDB.add(populated);

        BackendStub<?, ?, ?> backendStub;
        try {
          backendStub = backendStubFactory.create(jobService, populated);
        } catch (TransportPluginException e) {
          throw new TransactionException(e);
        }
        try {
          scheduler.addBackendStub(backendStub);
        } catch (BackendServiceException e) {
          logger.error("Failed to add Backend stub", e);
          throw new TransactionException(e);
        }
        logger.info("Backend {} registered.", populated.getId());
        return backend;
      }
    });
  }
  
  private <T extends Backend> T populate(T backend) {
    if (backend.getId() == null) {
      backend.setId(generateUniqueBackendId());
    }
    switch (backend.getType()) {
    case RABBIT_MQ:
      if (((BackendRabbitMQ) backend).getBackendConfiguration() == null) {
        String backendExchange = TransportConfigRabbitMQ.getBackendExchange(configuration);
        String backendExchangeType = TransportConfigRabbitMQ.getBackendExchangeType(configuration);
        String backendReceiveRoutingKey = TransportConfigRabbitMQ.getBackendReceiveRoutingKey(configuration);
        String backendReceiveControlRoutingKey = TransportConfigRabbitMQ.getBackendReceiveControlRoutingKey(configuration);
        Long heartbeatPeriodMills = TransportConfigRabbitMQ.getBackendHeartbeatTimeMills(configuration);

        backendExchange = backendExchange + "_" + backend.getId();
        BackendConfiguration backendConfiguration = new BackendConfiguration(backendExchange, backendExchangeType, backendReceiveRoutingKey, backendReceiveControlRoutingKey, heartbeatPeriodMills);
        ((BackendRabbitMQ) backend).setBackendConfiguration(backendConfiguration);
        return backend;
      }
      break;
    case ACTIVE_MQ:
      // TODO implement
      break;
    case LOCAL:
      // TODO implement
      break;
    default:
      break;
    }
    return backend;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Backend> T populate(String payload) {
    return (T) BeanSerializer.deserialize(payload, Backend.class);
  }
  
  private String generateUniqueBackendId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public void updateHeartbeatInfo(HeartbeatInfo info) throws TransactionException {
    backendRepository.updateHeartbeatInfo(info.getId(), new Timestamp(info.getTimestamp()));
  }

  @Override
  public Long getHeartbeatInfo(String id) {
    Timestamp timestamp = backendRepository.getHeartbeatInfo(id);
    return timestamp != null ? timestamp.getTime() : null;
  }
  
}
