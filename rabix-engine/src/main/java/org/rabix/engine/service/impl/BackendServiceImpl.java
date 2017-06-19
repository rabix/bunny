package org.rabix.engine.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.api.WorkerService;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.jvm.ClasspathScanner;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BackendServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.store.model.BackendRecord;
import org.rabix.engine.store.repository.BackendRepository;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.store.repository.TransactionHelper.TransactionException;
import org.rabix.engine.stub.BackendStub;
import org.rabix.engine.stub.BackendStubFactory;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;
import org.rabix.transport.backend.Backend.BackendType;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class BackendServiceImpl implements BackendService {

  private final static Logger logger = LoggerFactory.getLogger(BackendServiceImpl.class);

  private final SchedulerService scheduler;
  private final BackendStubFactory backendStubFactory;
  private final TransactionHelper transactionHelper;
  private final Configuration configuration;
  
  private final BackendRepository backendRepository;
  
  private final Injector injector;
  
  @Inject
  public BackendServiceImpl(BackendStubFactory backendStubFactory, SchedulerService backendDispatcher,
      TransactionHelper transactionHelper, BackendRepository backendRepository, Configuration configuration, Injector injector) {
    this.injector = injector;
    this.scheduler = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
    this.transactionHelper = transactionHelper;
    this.configuration = configuration;
    this.backendRepository = backendRepository;
  }
  
  @Override
  public void scanEmbedded() {
    Set<Class<WorkerService>> clazzes = ClasspathScanner.<WorkerService>scanInterfaceImplementations(WorkerService.class);

    int prefix = 1;
    for (Class<WorkerService> clazz : clazzes) {
      try {
        WorkerService backendAPI = clazz.newInstance();
        if (isEnabled(backendAPI.getType())) {
          injector.injectMembers(backendAPI);
          BackendLocal backendLocal = new BackendLocal(Integer.toString(prefix++));
          create(backendLocal);
          backendAPI.start(backendLocal);
        }
      } catch (InstantiationException | IllegalAccessException | BackendServiceException e) {
        logger.error("Failed to register backend " + clazz, e);
      }
    }
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
            BackendRecord br = new BackendRecord(
                backend.getId(),
                backend.getName(),
                Instant.now(),
                JSONHelper.convertToMap(backend),
                BackendRecord.Status.ACTIVE,
                BackendRecord.Type.valueOf(backend.getType().toString()));
            backendRepository.insert(br);
            BackendStub<?, ?, ?> backendStub = backendStubFactory.create(backend);
            scheduler.addBackendStub(backendStub);
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
    try {
      backendRepository.updateStatus(backend.getId(), BackendRecord.Status.ACTIVE);
      updateHeartbeatInfo(backend.getId(), Instant.now());
      scheduler.addBackendStub(backendStubFactory.create(backend));
    } catch (TransportPluginException e) {
      throw new BackendServiceException(e);
    }
  }
  
  private <T extends Backend> T populate(T backend) throws BackendServiceException {
    if (backend.getId() == null) {
      backend.setId(generateUniqueBackendId());
    }
    backend.setStatus(BackendStatus.ACTIVE);
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
  public void updateHeartbeatInfo(UUID id, Instant ts) throws BackendServiceException {
    backendRepository.updateHeartbeatInfo(id, ts);
  }

  @Override
  public void updateHeartbeatInfo(HeartbeatInfo info) throws BackendServiceException {
    backendRepository.updateHeartbeatInfo(info.getId(), Instant.ofEpochMilli(info.getTimestamp()));
  }

  @Override
  public Long getHeartbeatInfo(UUID id) {
    Instant timestamp = backendRepository.getHeartbeatInfo(id);
    return timestamp != null ? timestamp.toEpochMilli() : null;
  }

  @Override
  public List<Backend> getActiveBackends() {
    return backendRepository.getByStatus(BackendRecord.Status.ACTIVE).stream().map(
        br -> JSONHelper.convertToObject(br.getBackendConfig(), Backend.class)
    ).collect(Collectors.toList());
  }

  @Override
  public List<Backend> getActiveRemoteBackends() {
    return backendRepository.getByStatus(BackendRecord.Status.ACTIVE).stream().filter(b -> !b.getType().equals(BackendRecord.Type.LOCAL))
        .map(br -> JSONHelper.convertToObject(br.getBackendConfig(), Backend.class)).collect(Collectors.toList());
  }

  @Override
  public void stopBackend(Backend backend) throws BackendServiceException {
    backendRepository.updateStatus(backend.getId(), BackendRecord.Status.INACTIVE);
  }

  @Override
  public List<Backend> getAllBackends() {
    return backendRepository.getAll().stream().map(
        br -> JSONHelper.convertToObject(br.getBackendConfig(), Backend.class)
    ).collect(Collectors.toList());
  }

  @Override
  public boolean isEnabled(String type) {
    String[] backendTypes = configuration.getStringArray(BACKEND_TYPES_KEY);
    for (String backendType : backendTypes) {
      if (backendType.trim().equalsIgnoreCase(type)) {
        return true;
      }
    }
    return false;
  }
  
}
