package org.rabix.bindings.draft3;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessor;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.draft3.processor.callback.Draft3FilePathMapProcessorCallback;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public class Draft3FilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    
    Draft3PortProcessor draft2PortProcessor = new Draft3PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = draft2PortProcessor.processInputs(job.getInputs(), new Draft3FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    
    Draft3PortProcessor draft2PortProcessor = new Draft3PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(job.getOutputs(), new Draft3FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
