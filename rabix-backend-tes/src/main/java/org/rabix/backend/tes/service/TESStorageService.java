package org.rabix.backend.tes.service;

import java.nio.file.Path;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public interface TESStorageService {

  Job transformInputFiles(Job job) throws BindingException;
   
  Path workDir(Job job);

  Path localDir(Job job);

  List<FileValue> stageFile(Path workDir, FileValue fileValue) throws TESStorageException;

}
