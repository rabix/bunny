package org.rabix.transport.backend.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BackendSlurm extends BackendRabbitMQ{
    @Override
    @JsonIgnore
    public BackendType getType() {
        return BackendType.SLURM;
    }
}
