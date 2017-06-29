package org.rabix.backend.slurm.model;

import org.rabix.bindings.model.Job;

public class SlurmJob {
    private SlurmState jobStatus;

    public SlurmJob(SlurmState jobStatus) {
        this.jobStatus = jobStatus;
    }

    public SlurmState getJobStatus() { return jobStatus; }

    public void setJobStatus(SlurmState jobStatus) {
        this.jobStatus = jobStatus;
    }

    public SlurmState getState() {
        switch (jobStatus) {
            case Completed:
                return SlurmState.Completed;
            case Failed:
            case Stopped:
            case SpecialExit:
            case Timeout:
            case BootFail:
            case Cancelled:
            case Suspended:
                return SlurmState.Failed;
            default:
                return SlurmState.Unknown;
        }
    }

    public boolean isFinished() {
        switch (jobStatus) {
            case Failed:
            case Stopped:
            case SpecialExit:
            case Suspended:
            case Timeout:
            case Completed:
            case BootFail:
            case NodeFail:
                return true;
            default:
                return false;
        }
    }

    public static SlurmState convertToJobState(String jobStatus) {
        switch (jobStatus) {
            case "BF":
                return SlurmState.BootFail;
            case "CA":
                return SlurmState.Cancelled;
            case "CD":
                return SlurmState.Completed;
            case "CF":
                return SlurmState.Configuring;
            case "CG":
                return SlurmState.Completing;
            case "F":
                return SlurmState.Failed;
            case "NF":
                return SlurmState.NodeFail;
            case "PD":
                return SlurmState.Pending;
            case "PR":
                return SlurmState.Preempted;
            case "R":
                return SlurmState.Running;
            case "SE":
                return SlurmState.SpecialExit;
            case "ST":
                return SlurmState.Stopped;
            case "S":
                return SlurmState.Suspended;
            case "TO":
                return SlurmState.Timeout;
            default:
                return null;
        }
    }

    public static Job.JobStatus convertToJobStatus(SlurmState slurmState) {
        switch (slurmState) {
            case BootFail:
                return Job.JobStatus.FAILED;
            case Cancelled:
                return Job.JobStatus.ABORTED;
            case Completed:
                return Job.JobStatus.COMPLETED;
            case Configuring:
                return Job.JobStatus.STARTED;
            case Completing:
                return Job.JobStatus.STARTED;
            case Failed:
                return Job.JobStatus.FAILED;
            case NodeFail:
                return Job.JobStatus.FAILED;
            case Pending:
                return Job.JobStatus.PENDING;
            case Preempted:
                return Job.JobStatus.ABORTED;
            case Running:
                return Job.JobStatus.RUNNING;
            case SpecialExit:
                return Job.JobStatus.ABORTED;
            case Stopped:
                return Job.JobStatus.FAILED;
            case Suspended:
                return Job.JobStatus.ABORTED;
            case Timeout:
                return Job.JobStatus.FAILED;
            default:
                return null;
        }
    }
}