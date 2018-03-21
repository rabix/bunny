package org.rabix.engine.service;

import org.rabix.bindings.model.Job;

import java.util.UUID;

public interface IntermediaryFilesService {

  void registerOutputFiles(UUID rootId, Object value);

  void handleUnusedFilesIfAny(Job job);

  void handleJobFailed(Job job, Job rootJob);

  void decrementInputFilesReferences(UUID rootId, Object value);

  void incrementInputFilesReferences(UUID rootId, Object inputs);

  void freeze(UUID rootId, Object value);
}

