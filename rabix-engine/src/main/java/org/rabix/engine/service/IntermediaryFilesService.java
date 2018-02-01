package org.rabix.engine.service;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

import java.util.Set;
import java.util.UUID;

public interface IntermediaryFilesService {

  void addOrIncrement(UUID rootId, FileValue file, Integer usage);

  void decrementFiles(UUID rootId, Set<String> checkFiles);

  void jobFailed(UUID rootId, Set<String> rootInputs);

  void handleUnusedFiles(Job job);

  void handleJobCompleted(Job job);

  void handleJobFailed(Job job, Job rootJob);

  void extractPathsFromFileValue(Set<String> paths, FileValue file);

  void handleInputSent(UUID rootId, Object input);

  void handleInputSent(UUID rootId, Object input, int count);
}

