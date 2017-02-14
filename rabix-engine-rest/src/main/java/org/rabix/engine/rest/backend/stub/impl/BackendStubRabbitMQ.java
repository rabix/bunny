package org.rabix.engine.rest.backend.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;

public class BackendStubRabbitMQ extends BackendStub<TransportQueueRabbitMQ, BackendRabbitMQ, TransportPluginRabbitMQ> {

  public BackendStubRabbitMQ(JobService jobService, BackendRabbitMQ backend, Configuration configuration) throws TransportPluginException {
    this.jobService = jobService;
    this.backend = backend;

    this.transportPlugin = new TransportPluginRabbitMQ(configuration);

    BackendConfiguration backendConfiguration = backend.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey() + "_" + backend.getId());
    this.sendToBackendControlQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveControlRoutingKey() + "_" + backend.getId());
    
    EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
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

  @Override
  public void stop() {
    try {
      transportPlugin.deleteExchange(backend.getBackendConfiguration().getExchange());
    } catch (TransportPluginException e) {
      // do nothing
    }
  }

}
