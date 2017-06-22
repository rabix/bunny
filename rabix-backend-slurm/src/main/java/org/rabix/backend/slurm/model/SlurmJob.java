package org.rabix.backend.slurm.model;

public class SlurmJob{
    private String jobStatus;

    public SlurmJob(String jobStatus){
        this.jobStatus = jobStatus;
    }

    public String getJobStatus() { return jobStatus; }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public SlurmState getState(){
        switch (jobStatus) {
            case "CD":
                return SlurmState.Completed;
            case "F":
            case "ST":
            case "S":
            case "TO":
            case "R":
            case "NF":
            case "CA":
            case "BF":
                return SlurmState.Error;
            default:
                return SlurmState.Unknown;
        }
    }

    public boolean isFinished() {
        switch (jobStatus) {
            case "F": // failed
            case "ST":
            case "SE":
            case "S":
            case "TO":
            case "CD":
            case "BF":
            case "NF":
                return true;
            default:
                return false;
        }
    }

}