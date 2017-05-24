package org.rabix.backend.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.transport.backend.Backend;

public interface WorkerService {

  void initialize(Backend backend);
  
  void start(final Job job, UUID rootId);

  void stop(List<UUID> ids, UUID rootId);

  void free(UUID rootId, Map<String, Object> config);
  
  void shutdown(Boolean stopEverything);

  boolean isRunning(UUID id, UUID rootId);
  
  Map<String, Object> getResult(UUID id, UUID rootId);
  
  boolean isStopped();

  JobStatus findStatus(UUID id, UUID rootId);

}
