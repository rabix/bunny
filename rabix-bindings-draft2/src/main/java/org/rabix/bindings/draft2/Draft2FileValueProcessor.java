package org.rabix.bindings.draft2;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.draft2.processor.callback.Draft2PortProcessorHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;

public class Draft2FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).getInputFiles(job.getInputs(), null, null);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).getInputFiles(job.getInputs(), fileMapper, job.getConfig());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new Draft2PortProcessorHelper(draft2Job).getOutputFiles(job.getOutputs(), visiblePorts);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    Map<String, Object> inputs;
    try {
      inputs = new Draft2PortProcessorHelper(draft2Job).updateInputFiles(job.getInputs(), fileTransformer);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    Map<String, Object> outputs;
    try {
      outputs = new Draft2PortProcessorHelper(draft2Job).updateOutputFiles(job.getOutputs(), fileTransformer);
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenInputFiles(job.getInputs());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new Draft2PortProcessorHelper(draft2Job).flattenOutputFiles(job.getOutputs(), visiblePorts);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
