package org.rabix.engine.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.service.JobService;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.engine.stub.plugins.SlurmPlugin;

public class BackendStubSlurm extends BackendStubRabbitMQ{
    private SlurmPlugin slurmPlugin;

    public BackendStubSlurm(JobService jobService, BackendRabbitMQ backend, Configuration configuration) throws TransportPluginException {
        super(jobService, backend, configuration);
        this.slurmPlugin = new SlurmPlugin(backend, configuration);
    }

}
