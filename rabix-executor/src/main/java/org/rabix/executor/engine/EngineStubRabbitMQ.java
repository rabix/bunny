package org.rabix.executor.engine;

import org.apache.commons.configuration.Configuration;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;

public class EngineStubRabbitMQ extends EngineStub<TransportQueueRabbitMQ, BackendRabbitMQ, TransportPluginRabbitMQ> {

  public EngineStubRabbitMQ(BackendRabbitMQ backendRabbitMQ, ExecutorService executorService, Configuration configuration) throws TransportPluginException {
    this.backend = backendRabbitMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginRabbitMQ(configuration);
    
    this.heartbeatTimeMills = configuration.getLong("rabbitmq.backend.heartbeatTimeMills", DEFAULT_HEARTBEAT_TIME);
    
    BackendConfiguration backendConfiguration = backendRabbitMQ.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
    this.sendToBackendControlQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveControlRoutingKey());
    
    EngineConfiguration engineConfiguration = backendRabbitMQ.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());
    
    initialize();
  }
  
  /**
   * Try to initialize both exchanges (engine, backend)
   */
  private void initialize() {
    try {
      transportPlugin.initializeExchange(backend.getBackendConfiguration().getExchange(), backend.getBackendConfiguration().getExchangeType());
      transportPlugin.initializeExchange(backend.getEngineConfiguration().getExchange(), backend.getEngineConfiguration().getExchangeType());
    } catch (TransportPluginException e) {
      // do nothing
    }
  }
  
}
