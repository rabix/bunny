package org.rabix.bindings.sb;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.processor.SBPortProcessor;
import org.rabix.bindings.sb.processor.SBPortProcessorException;
import org.rabix.bindings.sb.processor.callback.SBFilePathMapProcessorCallback;

public class SBFilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    
    SBPortProcessor sbPortProcessor = new SBPortProcessor(sbJob);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = sbPortProcessor.processInputs(sbJob.getInputs(), new SBFilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) SBValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    
    SBPortProcessor sbPortProcessor = new SBPortProcessor(sbJob);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = sbPortProcessor.processOutputs(sbJob.getOutputs(), new SBFilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) SBValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
