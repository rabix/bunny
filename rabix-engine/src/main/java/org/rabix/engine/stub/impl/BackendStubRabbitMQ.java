package org.rabix.engine.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.JobService;
import org.rabix.engine.stub.BackendStub;
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
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
    this.sendToBackendControlQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveControlRoutingKey());
    
    EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());

    this.enableControlMesages = configuration.getBoolean("bunny.enable_backend_control_messages", false);
    this.cleanup = configuration.getBoolean("cleaner.remove_queues", false);
    initialize();
  }

  /**
   * Try to initialize both exchanges (engine, backend)
   */
  private void initialize() {
    try {
      transportPlugin.initializeExchange(backend.getBackendConfiguration().getExchange(), backend.getBackendConfiguration().getExchangeType());
      transportPlugin.initializeExchange(backend.getEngineConfiguration().getExchange(), backend.getEngineConfiguration().getExchangeType());
      transportPlugin.initQueue(sendToBackendQueue);
      transportPlugin.initQueue(receiveFromBackendQueue);
      transportPlugin.initQueue(receiveFromBackendHeartbeatQueue);
      if(enableControlMesages)
        transportPlugin.initQueue(sendToBackendControlQueue);
    } catch (TransportPluginException e) {
      // do nothing
    }
  }

  @Override
  public void stop() {
    super.stop();
    if(this.cleanup){
      transportPlugin.deleteQueue(sendToBackendQueue.getQueueName());
      transportPlugin.deleteQueue(receiveFromBackendHeartbeatQueue.getQueueName());
      transportPlugin.deleteQueue(receiveFromBackendQueue.getQueueName());
      if(enableControlMesages)
        transportPlugin.deleteQueue(sendToBackendControlQueue.getQueueName());
    }
  }

}
