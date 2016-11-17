package org.rabix.bindings.cwl;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.callback.CWLPortProcessorHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;

public class CWLFileValueProcessor implements ProtocolFileValueProcessor {

  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      return new CWLPortProcessorHelper(cwlJob).getInputFiles(cwlJob.getInputs(), fileMapper, job.getConfig());
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Map<String, Object> inputs;
    try {
      inputs = new CWLPortProcessorHelper(cwlJob).updateInputFiles(cwlJob.getInputs(), fileTransformer);
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(job, commonInputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Map<String, Object> outputs;
    try {
      outputs = new CWLPortProcessorHelper(cwlJob).updateOutputFiles(cwlJob.getOutputs(), fileTransformer);
      @SuppressWarnings("unchecked")
      Map<String, Object> commonOutputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
