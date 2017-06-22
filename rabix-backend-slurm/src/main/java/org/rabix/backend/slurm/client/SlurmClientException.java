package org.rabix.backend.slurm.client;

public class SlurmClientException extends Exception{

    private static final long serialVersionUID = -3341213522183821325L;

    public SlurmClientException(String message) {
        super(message);
    }

    public SlurmClientException(String message, Throwable e) {
        super(message, e);
    }

    public SlurmClientException(Throwable e) {
        super(e);
    }

}

