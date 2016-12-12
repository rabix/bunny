package org.rabix.engine.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.Job;

public class ReadyJobGroupsDB {
  
  private ConcurrentMap<String, Map<String, Set<Job>>> groupedJobs;

  public ReadyJobGroupsDB() {
    groupedJobs = new ConcurrentHashMap<>();
  }

  public void add(String eventId, Job job) {
    Map<String, Set<Job>> groupedByRoot = groupedJobs.get(job.getRootId());

    if (groupedByRoot == null) {
      groupedByRoot = new HashMap<>();
      groupedJobs.put(job.getRootId(), groupedByRoot);
    }

    Set<Job> jobs = groupedByRoot.get(eventId);
    if (jobs == null) {
      jobs = new HashSet<>();
      groupedByRoot.put(eventId, jobs);
    }
    jobs.add(job);
  }

  public Set<Job> get(String rootId, String eventId) {
    Map<String, Set<Job>> groupedByRoot = groupedJobs.get(rootId);
    if (groupedByRoot == null) {
      return Collections.<Job> emptySet();
    }
    return groupedByRoot.containsKey(eventId) ? groupedByRoot.get(eventId) : Collections.<Job> emptySet();
  }

  public void delete(String rootId) {
    groupedJobs.remove(rootId);
  }
}
