package org.rabix.engine.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.BackendRepository.BackendStatus;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BackendServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.stub.BackendStub;
import org.rabix.engine.stub.BackendStubFactory;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendServiceImpl implements BackendService {

  private final static Logger logger = LoggerFactory.getLogger(BackendServiceImpl.class);
  
  private final SchedulerService scheduler;
  private final BackendStubFactory backendStubFactory;
  private final TransactionHelper transactionHelper;
  private final Configuration configuration;
  
  private final BackendRepository backendRepository;

  @Inject
  public BackendServiceImpl(BackendStubFactory backendStubFactory, SchedulerService backendDispatcher,
      TransactionHelper transactionHelper, BackendRepository backendRepository, Configuration configuration) {
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
      backendStub = backendStubFactory.create(backend);
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
        BackendRabbitMQ backendRabbitMQ = (BackendRabbitMQ) backend;
        String idPostfix = "_" + backend.getId();
        if (backendRabbitMQ.getBackendConfiguration() == null) {
          String backendExchange = TransportConfigRabbitMQ.getBackendExchange(configuration);
          String backendExchangeType = TransportConfigRabbitMQ.getBackendExchangeType(configuration);
          String backendReceiveRoutingKey = TransportConfigRabbitMQ.getBackendReceiveRoutingKey(configuration) + idPostfix;
          String backendReceiveControlRoutingKey = TransportConfigRabbitMQ.getBackendReceiveControlRoutingKey(configuration) + idPostfix;
          Long heartbeatPeriodMills = TransportConfigRabbitMQ.getBackendHeartbeatTimeMills(configuration);

          BackendConfiguration backendConfiguration = new BackendConfiguration(backendExchange, backendExchangeType, backendReceiveRoutingKey,
              backendReceiveControlRoutingKey, heartbeatPeriodMills);
          backendRabbitMQ.setBackendConfiguration(backendConfiguration);
        }
        if (backendRabbitMQ.getEngineConfiguration() == null) {
          String rabbitEngineExchange = configuration.getString("rabbitmq.engine.exchange");
          String rabbitEngineExchangeType = configuration.getString("rabbitmq.engine.exchangeType");
          String rabbitEngineReceiveRoutingKey = configuration.getString("rabbitmq.engine.receiveRoutingKey") + idPostfix;
          String rabbitEngineHeartbeatRoutingKey = configuration.getString("rabbitmq.engine.heartbeatRoutingKey") + idPostfix;

          EngineConfiguration engineConfiguration = new EngineConfiguration(rabbitEngineExchange, rabbitEngineExchangeType, rabbitEngineReceiveRoutingKey,
              rabbitEngineHeartbeatRoutingKey);
          backendRabbitMQ.setEngineConfiguration(engineConfiguration);
          }
        return backend;
      
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
  
  private UUID generateUniqueBackendId() {
    return UUID.randomUUID();
  }
  
  @Override
  public void updateHeartbeatInfo(UUID id, Timestamp ts) throws BackendServiceException {
    backendRepository.updateHeartbeatInfo(id, ts);
  }

  @Override
  public void updateHeartbeatInfo(HeartbeatInfo info) throws BackendServiceException {
    backendRepository.updateHeartbeatInfo(info.getId(), new Timestamp(info.getTimestamp()));
  }

  @Override
  public Long getHeartbeatInfo(UUID id) {
    Timestamp timestamp = backendRepository.getHeartbeatInfo(id);
    return timestamp != null ? timestamp.getTime() : null;
  }

  @Override
  public List<Backend> getActiveBackends() {
    return backendRepository.getByStatus(BackendStatus.ACTIVE);
  }

  @Override
  public void stopBackend(Backend backend) throws BackendServiceException {
    backendRepository.updateStatus(backend.getId(), BackendStatus.INACTIVE);
  }
  
}
