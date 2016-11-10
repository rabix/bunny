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
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    
    Draft3PortProcessor draft3PortProcessor = new Draft3PortProcessor(draft3Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = draft3PortProcessor.processInputs(draft3Job.getInputs(), new Draft3FilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    
    Draft3PortProcessor draft3PortProcessor = new Draft3PortProcessor(draft3Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = draft3PortProcessor.processOutputs(draft3Job.getOutputs(), new Draft3FilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
