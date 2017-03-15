package org.rabix.executor.engine;

import org.apache.commons.configuration.Configuration;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.activemq.TransportPluginActiveMQ;
import org.rabix.transport.mechanism.impl.activemq.TransportQueueActiveMQ;

public class EngineStubActiveMQ extends EngineStub<TransportQueueActiveMQ, BackendActiveMQ, TransportPluginActiveMQ> {

  public EngineStubActiveMQ(BackendActiveMQ backendActiveMQ, ExecutorService executorService, Configuration configuration) throws TransportPluginException {
    this.backend = backendActiveMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginActiveMQ(configuration);
    
    this.heartbeatTimeMills = configuration.getLong("rabbitmq.backend.heartbeatTimeMills", DEFAULT_HEARTBEAT_TIME);
    
    this.sendToBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getToBackendQueue());
    this.sendToBackendControlQueue = new TransportQueueActiveMQ(backendActiveMQ.getToBackendControlQueue());
    this.receiveFromBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendHeartbeatQueue());
  }
  
}
