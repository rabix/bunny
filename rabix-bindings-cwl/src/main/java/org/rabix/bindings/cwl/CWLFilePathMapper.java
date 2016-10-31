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
      Map<String, Object> inputs = cwlPortProcessor.processInputs(job.getInputs(), new CWLFilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithInputs(job, inputs);
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
      Map<String, Object> outputs = cwlPortProcessor.processOutputs(job.getOutputs(), new CWLFilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
