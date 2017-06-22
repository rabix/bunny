package org.rabix.backend.slurm;

public class SlurmServiceException extends Exception{

    private static final long serialVersionUID = -2457813832183821325L;

    public SlurmServiceException(String message) {
        super(message);
    }

    public SlurmServiceException(String message, Throwable e) {
        super(message, e);
    }

    public SlurmServiceException(Throwable e) {
        super(e);
    }

}

