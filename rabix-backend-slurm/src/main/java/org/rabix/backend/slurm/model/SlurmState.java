package org.rabix.backend.slurm.model;

public enum SlurmState {
    Unknown,
    Queued,
    Running,
    Paused,
    Completed,
    Error,
    SystemError,
    Canceled,
    Initializing
}