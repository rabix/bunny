package org.rabix.backend.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.transport.backend.Backend;

public interface WorkerService {

  void start(Backend backend);
  
  void submit(final Job job, UUID rootId);

  void cancel(List<UUID> ids, UUID rootId);

  void freeResources(UUID rootId, Map<String, Object> config);
  
  void shutdown(Boolean stopEverything);

  boolean isRunning(UUID id, UUID rootId);
  
  Map<String, Object> getResult(UUID id, UUID rootId);
  
  boolean isStopped();

  JobStatus findStatus(UUID id, UUID rootId);
  
  String getType();

}
