package org.rabix.engine.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.JobService;
import org.rabix.engine.stub.BackendStub;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.local.TransportPluginLocal;
import org.rabix.transport.mechanism.impl.local.TransportQueueLocal;

public class BackendStubLocal extends BackendStub<TransportQueueLocal, BackendLocal, TransportPluginLocal> {

  public BackendStubLocal(JobService jobService, Configuration configuration, BackendLocal backendLocal) throws TransportPluginException {
    this.jobService = jobService;
    this.backend = backendLocal;
    this.transportPlugin = new TransportPluginLocal(configuration);

    this.sendToBackendQueue = new TransportQueueLocal(backendLocal.getToBackendQueue());
    this.sendToBackendControlQueue = new TransportQueueLocal(backendLocal.getToBackendControlQueue());
    this.receiveFromBackendQueue = new TransportQueueLocal(backendLocal.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueLocal(backendLocal.getFromBackendHeartbeatQueue());
  }

}
