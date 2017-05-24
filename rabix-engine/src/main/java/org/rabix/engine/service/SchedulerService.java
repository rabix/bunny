package org.rabix.engine.service;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.engine.service.SchedulerService.SchedulerJobBackendAssigner.JobBackendAssignment;
import org.rabix.engine.stub.BackendStub;
import org.rabix.transport.backend.Backend;

public interface SchedulerService {

  void start();
  
  boolean stop(Job... jobs);

  void addBackendStub(BackendStub<?, ?, ?> backendStub) throws BackendServiceException;

  void freeBackend(Job rootJob);

  void deallocate(Job job);
  
  @FunctionalInterface
  public static interface SchedulerJobBackendAssigner {
    
    public class JobBackendAssignment {
      private final Job job;
      private final Backend backend;
      
      public JobBackendAssignment(Job job, Backend backend) {
        super();
        this.job = job;
        this.backend = backend;
      }
      
      public Backend getBackend() {
        return backend;
      }
      
      public Job getJob() {
        return job;
      }

      @Override
      public String toString() {
        return "JobBackendAssignment [jobId=" + job.getId() + ", backendId=" + backend.getId() + "]";
      }
    }
    
    Set<JobBackendAssignment> assign(final Set<Job> jobs, final Set<Backend> backends);
  }
  
  @FunctionalInterface
  public static interface SchedulerMessageCreator {
    Set<SchedulerMessage> create(Set<JobBackendAssignment> entities, Set<UUID> backendIds);
  }
  
  @FunctionalInterface
  public static interface SchedulerMessageSender {
    void send(Set<SchedulerMessage> messages);
  }
  
  public static class SchedulerMessage {

    private Object payload;
    private UUID backendId;
    
    public SchedulerMessage(UUID backendId, Object payload) {
      super();
      this.backendId = backendId;
      this.payload = payload;
    }
    
    public UUID getBackendId() {
      return backendId;
    }
    
    public Object getPayload() {
      return payload;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((backendId == null) ? 0 : backendId.hashCode());
      result = prime * result + ((payload == null) ? 0 : payload.hashCode());
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
      SchedulerMessage other = (SchedulerMessage) obj;
      if (backendId == null) {
        if (other.backendId != null)
          return false;
      } else if (!backendId.equals(other.backendId))
        return false;
      if (payload == null) {
        if (other.payload != null)
          return false;
      } else if (!payload.equals(other.payload))
        return false;
      return true;
    }
  }
  
}
