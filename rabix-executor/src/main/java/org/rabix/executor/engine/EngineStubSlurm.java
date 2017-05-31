package org.rabix.executor.engine;

import org.apache.commons.configuration.Configuration;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;

public class EngineStubSlurm extends EngineStubRabbitMQ{

    public EngineStubSlurm(BackendRabbitMQ backendRabbitMQ, ExecutorService executorService, Configuration configuration) throws TransportPluginException{
        super(backendRabbitMQ, executorService, configuration);
    }
}
