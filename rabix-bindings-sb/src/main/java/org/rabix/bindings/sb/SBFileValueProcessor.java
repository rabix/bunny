package org.rabix.bindings.sb;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorException;
import org.rabix.bindings.sb.processor.callback.SBPortProcessorHelper;
import org.rabix.bindings.transformer.FileTransformer;

public class SBFileValueProcessor implements ProtocolFileValueProcessor {

  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).getInputFiles(sbJob.getInputs(), fileMapper, job.getConfig());
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    Map<String, Object> inputs;
    try {
      inputs = new SBPortProcessorHelper(sbJob).updateInputFiles(sbJob.getInputs(), fileTransformer);
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) SBValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    Map<String, Object> outputs;
    try {
      outputs = new SBPortProcessorHelper(sbJob).updateOutputFiles(sbJob.getOutputs(), fileTransformer);
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) SBValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
