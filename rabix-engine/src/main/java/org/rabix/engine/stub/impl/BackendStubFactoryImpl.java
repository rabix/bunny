package org.rabix.engine.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.JobService;
import org.rabix.engine.stub.BackendStub;
import org.rabix.engine.stub.BackendStubFactory;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendSlurm;
import org.rabix.transport.mechanism.TransportPluginException;

import com.google.inject.Inject;

public class BackendStubFactoryImpl implements BackendStubFactory {

  private Configuration configuration;
  private JobService jobService;

  @Inject
  public BackendStubFactoryImpl(JobService jobService,Configuration configuration) {
    this.jobService = jobService;
    this.configuration = configuration;
  }

  /* (non-Javadoc)
   * @see org.rabix.engine.stub.impl.BackendStubFactory#create(T)
   */
  @Override
  public <T extends Backend> BackendStub<?, ?, ?> create(T backend) throws TransportPluginException {
    switch (backend.getType()) {
    case ACTIVE_MQ:
      return new BackendStubActiveMQ(jobService, configuration, (BackendActiveMQ) backend);
    case LOCAL:
      return new BackendStubLocal(jobService, configuration, (BackendLocal) backend);
    case RABBIT_MQ:
      return new BackendStubRabbitMQ(jobService, (BackendRabbitMQ) backend, configuration);
    case SLURM:
      return new BackendStubSlurm(jobService, (BackendSlurm) backend, configuration);
    default:
      break;
    }
    throw new TransportPluginException("There is no Backend stub for " + backend);
  }

}
