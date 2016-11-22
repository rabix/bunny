package org.rabix.bindings.draft3;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.draft3.processor.callback.Draft3PortProcessorHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;

public class Draft3FileValueProcessor implements ProtocolFileValueProcessor {

  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft3Job).getInputFiles(draft3Job.getInputs(), fileMapper, job.getConfig());
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> inputs;
    try {
      inputs = new Draft3PortProcessorHelper(draft3Job).updateInputFiles(draft3Job.getInputs(), fileTransformer);
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> outputs;
    try {
      outputs = new Draft3PortProcessorHelper(draft3Job).updateOutputFiles(draft3Job.getOutputs(), fileTransformer);
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
