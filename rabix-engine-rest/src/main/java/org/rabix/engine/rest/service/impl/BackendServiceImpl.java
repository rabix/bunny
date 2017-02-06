package org.rabix.engine.rest.service.impl;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.BackendRepository.BackendStatus;
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
  
  private final JobService jobService;
  private final SchedulerService scheduler;
  private final BackendStubFactory backendStubFactory;
  private final TransactionHelper transactionHelper;
  private final Configuration configuration;
  
  private final BackendRepository backendRepository;

  @Inject
  public BackendServiceImpl(JobService jobService, BackendStubFactory backendStubFactory,
      SchedulerService backendDispatcher, TransactionHelper transactionHelper, BackendRepository backendRepository,
      Configuration configuration) {
    this.jobService = jobService;
    this.scheduler = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
    this.transactionHelper = transactionHelper;
    this.configuration = configuration;
    this.backendRepository = backendRepository;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Backend> T create(T backend) throws BackendServiceException {
    try {
      return (T) transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Backend>() {
        @Override
        public Backend call() throws Exception {
          try {
            Backend populated = populate(backend);
            backendRepository.insert(backend.getId(), backend, new Timestamp(System.currentTimeMillis()), BackendStatus.ACTIVE);
            startBackend(populated);
            logger.info("Backend {} registered.", populated.getId());
            return backend;
          } catch (BackendServiceException e) {
            throw new TransactionException(e);
          }
        }
      });
    } catch (Exception e) {
      throw new BackendServiceException(e);
    }
  }
  
  public void startBackend(Backend backend) throws BackendServiceException {
    BackendStub<?, ?, ?> backendStub;
    try {
      backendStub = backendStubFactory.create(jobService, backend);
    } catch (TransportPluginException e) {
      throw new BackendServiceException(e);
    }
    scheduler.addBackendStub(backendStub);
  }
  
  private <T extends Backend> T populate(T backend) throws BackendServiceException {
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
    case LOCAL:
      break;
    default:
      throw new BackendServiceException("Unknown backend type " + backend.getType());
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
  public void updateHeartbeatInfo(HeartbeatInfo info) throws BackendServiceException {
    backendRepository.updateHeartbeatInfo(info.getId(), new Timestamp(info.getTimestamp()));
  }

  @Override
  public Long getHeartbeatInfo(String id) {
    Timestamp timestamp = backendRepository.getHeartbeatInfo(id);
    return timestamp != null ? timestamp.getTime() : null;
  }

  @Override
  public void stopBackend(Backend backend) throws BackendServiceException {
    backendRepository.updateStatus(backend.getId(), BackendStatus.INACTIVE);
  }
  
}
