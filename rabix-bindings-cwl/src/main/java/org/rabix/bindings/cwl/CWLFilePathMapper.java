package org.rabix.bindings.cwl;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessor;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.callback.CWLFilePathMapProcessorCallback;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;

public class CWLFilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    
    CWLPortProcessor cwlPortProcessor = new CWLPortProcessor(cwlJob);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = cwlPortProcessor.processInputs(cwlJob.getInputs(), new CWLFilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FilePathMapper fileMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    
    CWLPortProcessor cwlPortProcessor = new CWLPortProcessor(cwlJob);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = cwlPortProcessor.processOutputs(cwlJob.getOutputs(), new CWLFilePathMapProcessorCallback(fileMapper, config));
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
