package org.rabix.bindings;

import java.io.File;
import java.util.List;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public interface ProtocolCommandLineBuilder {

  String buildCommandLine(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;
  
  List<String> buildCommandLineParts(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;
  
  CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;
}
