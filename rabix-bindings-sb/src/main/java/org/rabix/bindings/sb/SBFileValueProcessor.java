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

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).getInputFiles(job.getInputs(), null, null);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).getInputFiles(job.getInputs(), fileMapper, job.getConfig());
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new SBPortProcessorHelper(sbJob).getOutputFiles(job.getOutputs(), visiblePorts);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    SBJob draft2Job = SBJobHelper.getSBJob(job);
    Map<String, Object> inputs;
    try {
      inputs = new SBPortProcessorHelper(draft2Job).updateInputFiles(job.getInputs(), fileTransformer);
      return Job.cloneWithInputs(job, inputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    SBJob draft2Job = SBJobHelper.getSBJob(job);
    Map<String, Object> outputs;
    try {
      outputs = new SBPortProcessorHelper(draft2Job).updateOutputFiles(job.getOutputs(), fileTransformer);
      return Job.cloneWithOutputs(job, outputs);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).flattenInputFiles(job.getInputs());
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new SBPortProcessorHelper(sbJob).flattenOutputFiles(job.getOutputs(), visiblePorts);
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
