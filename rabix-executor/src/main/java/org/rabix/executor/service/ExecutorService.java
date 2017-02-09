package org.rabix.executor.service;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.transport.backend.Backend;

public interface ExecutorService {

  void initialize(Backend backend);
  
  void start(final Job job, String contextId);

  void stop(List<String> ids, String contextId);

  void free(String rootId, Map<String, Object> config);
  
  void shutdown(Boolean stopEverything);

  boolean isRunning(String id, String contextId);
  
  Map<String, Object> getResult(String id, String contextId);
  
  boolean isStopped();

  JobStatus findStatus(String id, String contextId);

}
