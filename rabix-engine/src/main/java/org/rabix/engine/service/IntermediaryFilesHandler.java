package org.rabix.engine.service;

import java.util.Set;

import org.rabix.bindings.model.Job;

public interface IntermediaryFilesHandler {

 public void handleUnusedFiles(Job job, Set<String> unusedFiles);

}
