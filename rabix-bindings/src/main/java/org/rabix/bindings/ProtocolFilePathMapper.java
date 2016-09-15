package org.rabix.bindings;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public interface ProtocolFilePathMapper {

  Job mapInputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException;
  
  Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException;
  
}
