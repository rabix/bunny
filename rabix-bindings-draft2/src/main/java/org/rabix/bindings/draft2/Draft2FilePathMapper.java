package org.rabix.bindings.draft2;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessor;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.draft2.processor.callback.Draft2FilePathMapProcessorCallback;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public class Draft2FilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = draft2PortProcessor.processInputs(job.getInputs(), new Draft2FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(job.getOutputs(), new Draft2FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
