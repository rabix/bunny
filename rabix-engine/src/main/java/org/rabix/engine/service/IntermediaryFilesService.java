package org.rabix.engine.service;

import org.rabix.bindings.model.Job;

public interface IntermediaryFilesService {

  void handleUnusedFilesIfAny(Job job);

  void handleJobFailed(Job job, Job rootJob);

  void decrementInputFilesReferences(Job job);

  void decrementOutputFilesReferences(Job job);

  void incrementInputFilesReferences(Job job);
}

