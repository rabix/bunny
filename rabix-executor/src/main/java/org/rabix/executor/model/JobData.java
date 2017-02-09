package org.rabix.executor.model;

import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;

public class JobData {

  public static enum JobDataStatus {
    PENDING,
    READY,
    STARTED,

    ABORTING,
    ABORTED,

    FAILED,
    COMPLETED,
    RUNNING;

    public static JobDataStatus convertFromJobStatus(JobStatus status) {
      switch (status) {
      case PENDING:
        return JobDataStatus.PENDING;
      case READY:
        return JobDataStatus.READY;
      case STARTED:
        return JobDataStatus.STARTED;
      case ABORTED:
        return JobDataStatus.ABORTED;
      case FAILED:
        return JobDataStatus.FAILED;
      case COMPLETED:
        return JobDataStatus.COMPLETED;
      case RUNNING:
        return JobDataStatus.RUNNING;
      default:
        return null;
      }
    }

    public static JobStatus convertToJobStatus(JobDataStatus status) {
      switch (status) {
      case PENDING:
        return JobStatus.PENDING;
      case READY:
        return JobStatus.READY;
      case STARTED:
        return JobStatus.STARTED;
      case ABORTED:
        return JobStatus.ABORTED;
      case ABORTING:
        return JobStatus.ABORTED;
      case FAILED:
        return JobStatus.FAILED;
      case COMPLETED:
        return JobStatus.COMPLETED;
      case RUNNING:
        return JobStatus.RUNNING;
      default:
        return null;
      }
    }
  }
  
  private final Job job;
  private final JobDataStatus status;
  private final Map<String, Object> result;
  private final String message;
  private final boolean important;
  private final boolean terminal;
  private final boolean logsUploaded;

  public JobData(Job job, JobDataStatus status, String message, boolean important, boolean terminal) {
    this.job = job;
    this.status = status;
    this.important = important;
    this.terminal = terminal;
    this.logsUploaded = false;
    this.result = null;
    this.message = message;
  }
  
  public JobData(Job job, JobDataStatus status, String message, Map<String, Object> result, boolean important, boolean terminal) {
    this.job = job;
    this.status = status;
    this.important = important;
    this.terminal = terminal;
    this.logsUploaded = false;
    this.result = result;
    this.message = message;
  }
  
  public static JobData cloneWithJob(JobData jobData, Job job) {
    return new JobData(job, jobData.status, jobData.message, jobData.result, jobData.important, jobData.terminal);
  }
  
  public static JobData cloneWithResult(JobData jobData, Map<String, Object> result) {
    return new JobData(jobData.job, jobData.status, jobData.message, result, jobData.important, jobData.terminal);
  }
  
  public static JobData cloneWithStatus(JobData jobData, JobDataStatus status) {
    return new JobData(jobData.job, status, jobData.message, jobData.important, jobData.terminal);
  }
  
  public static JobData cloneWithStatusAndMessage(JobData jobData, JobDataStatus status, String message) {
    return new JobData(jobData.job, status, message, jobData.important, jobData.terminal);
  }

  public String getId() {
    return job.getId();
  }
  
  public Job getJob() {
    return job;
  }

  public JobDataStatus getStatus() {
    return status;
  }

  public Map<String, Object> getResult() {
    return result;
  }

  public String getMessage() {
    return message;
  }

  public boolean isImportant() {
    return important;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public boolean isLogsUploaded() {
    return logsUploaded;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((job == null) ? 0 : job.hashCode());
    result = prime * result + (important ? 1231 : 1237);
    result = prime * result + (logsUploaded ? 1231 : 1237);
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + (terminal ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JobData other = (JobData) obj;
    if (job == null) {
      if (other.job != null)
        return false;
    } else if (!job.equals(other.job))
      return false;
    if (important != other.important)
      return false;
    if (logsUploaded != other.logsUploaded)
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (status != other.status)
      return false;
    if (terminal != other.terminal)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobData [jobId=" + job.getId() + ", status=" + status + ", message=" + message + ", important="  + important + ", terminal=" + terminal + ", logsUploaded=" + logsUploaded + "]";
  }
}
