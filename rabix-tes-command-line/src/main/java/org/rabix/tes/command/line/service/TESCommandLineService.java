package org.rabix.tes.command.line.service;

import java.io.File;

import org.rabix.bindings.model.Job;

public interface TESCommandLineService {

  void execute(Job job, File workingDir) throws TESCommandLineException;
  
}
