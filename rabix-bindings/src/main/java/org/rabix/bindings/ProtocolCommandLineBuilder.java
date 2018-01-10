package org.rabix.bindings;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public interface ProtocolCommandLineBuilder {

  String buildCommandLine(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;

  default String buildCommandLine(Job job, Path workingDir, FilePathMapper filePathMapper) throws BindingException {
    return this.buildCommandLine(job, workingDir.toFile(), filePathMapper);
  }

  List<String> buildCommandLineParts(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;

  default List<String> buildCommandLineParts(Job job, Path workingDir, FilePathMapper filePathMapper) throws BindingException {
    return this.buildCommandLineParts(job, workingDir.toFile(), filePathMapper);
  }

  CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;

  default CommandLine buildCommandLineObject(Job job, Path workingDir, FilePathMapper filePathMapper) throws BindingException {
    return this.buildCommandLineObject(job, workingDir.toFile(), filePathMapper);
  }
}
