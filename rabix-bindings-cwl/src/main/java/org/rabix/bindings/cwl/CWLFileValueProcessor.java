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

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      return new CWLPortProcessorHelper(cwlJob).getInputFiles(job.getInputs(), null, null);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      return new CWLPortProcessorHelper(cwlJob).getInputFiles(job.getInputs(), fileMapper, job.getConfig());
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new CWLPortProcessorHelper(cwlJob).getOutputFiles(job.getOutputs(), visiblePorts);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Map<String, Object> inputs;
    try {
      inputs = new CWLPortProcessorHelper(cwlJob).updateInputFiles(job.getInputs(), fileTransformer);
      return Job.cloneWithInputs(job, inputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Map<String, Object> outputs;
    try {
      outputs = new CWLPortProcessorHelper(cwlJob).updateOutputFiles(job.getOutputs(), fileTransformer);
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWLPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
