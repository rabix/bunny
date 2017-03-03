package org.rabix.engine.stub;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.JobService;
import org.rabix.engine.stub.impl.BackendStubActiveMQ;
import org.rabix.engine.stub.impl.BackendStubLocal;
import org.rabix.engine.stub.impl.BackendStubRabbitMQ;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;

import com.google.inject.Inject;

public class BackendStubFactory {

  private Configuration configuration;

  @Inject
  public BackendStubFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  public <T extends Backend> BackendStub<?, ?, ?> create(JobService jobService, T backend) throws TransportPluginException {
    switch (backend.getType()) {
    case ACTIVE_MQ:
      return new BackendStubActiveMQ(jobService, configuration, (BackendActiveMQ) backend);
    case LOCAL:
      return new BackendStubLocal(jobService, configuration, (BackendLocal) backend);
    case RABBIT_MQ:
      return new BackendStubRabbitMQ(jobService, (BackendRabbitMQ) backend, configuration);
    default:
      break;
    }
    throw new TransportPluginException("There is no Backend stub for " + backend);
  }

}
