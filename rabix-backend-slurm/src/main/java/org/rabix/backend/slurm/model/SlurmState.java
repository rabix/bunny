package org.rabix.backend.slurm.model;

public enum SlurmState {
    BootFail,
    Cancelled,
    Completed,
    Configuring,
    Completing,
    Failed,
    NodeFail,
    Pending,
    Preempted,
    Running,
    SpecialExit,
    Stopped,
    Suspended,
    Timeout,
    Unknown
}