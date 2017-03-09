package org.rabix.engine.service;

import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public interface IntermediaryFilesService {

  void addOrIncrement(UUID rootId, FileValue file, Integer usage);

  void decrementFiles(UUID rootId, Set<String> checkFiles);

  void jobFailed(UUID rootId, Set<String> rootInputs);

  void handleUnusedFiles(Job job);

  void dumpFiles();

  void handleContainerReady(Job containerJob, boolean keepInputFiles);

  void handleJobCompleted(Job job);

  void handleJobFailed(Job job, Job rootJob, boolean keepInputFiles);

  void extractPathsFromFileValue(Set<String> paths, FileValue file);

}