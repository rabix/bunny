package org.rabix.bindings.sb.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.processor.SBPortProcessor;
import org.rabix.bindings.sb.processor.SBPortProcessorException;
import org.rabix.bindings.transformer.FileTransformer;

public class SBPortProcessorHelper {

  private final SBJob sbJob;
  private final SBPortProcessor portProcessor;

  public SBPortProcessorHelper(SBJob sbJob) {
    this.sbJob = sbJob;
    this.portProcessor = new SBPortProcessor(sbJob);
  }

  public Set<FileValue> getInputFiles(Map<String, Object> inputs, FilePathMapper fileMapper, Map<String, Object> config) throws SBPortProcessorException {
    if (fileMapper != null) {
      SBFilePathMapProcessorCallback fileMapperCallback = new SBFilePathMapProcessorCallback(fileMapper, config);
      inputs = portProcessor.processInputs(inputs, fileMapperCallback);
    }
    
    SBFileValueProcessorCallback callback = new SBFileValueProcessorCallback(sbJob, null, true);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to get input files.", e);
    }
    return callback.getFileValues();
  }
  
  public Set<FileValue> getOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws SBPortProcessorException {
    SBFileValueProcessorCallback callback = new SBFileValueProcessorCallback(sbJob, visiblePorts, false);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to get output files.", e);
    }
    return callback.getFileValues();
  }
  
  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFilePathFlattenProcessorCallback callback = new SBFilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFileValueFlattenProcessorCallback callback = new SBFileValueFlattenProcessorCallback(null);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws SBPortProcessorException {
    SBFileValueFlattenProcessorCallback callback = new SBFileValueFlattenProcessorCallback(visiblePorts);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedFileData();
  }
  
  public Map<String, Object> updateInputFiles(Map<String, Object> inputs, FileTransformer fileTransformer) throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBFileValueUpdateProcessorCallback(fileTransformer));
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> updateOutputFiles(Map<String, Object> outputs, FileTransformer fileTransformer) throws SBPortProcessorException {
    try {
      return portProcessor.processOutputs(outputs, new SBFileValueUpdateProcessorCallback(fileTransformer));
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to set input file size", e);
    }
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws SBPortProcessorException {
    SBFilePathFlattenProcessorCallback callback = new SBFilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFileDataFlattenProcessorCallback callback = new SBFileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws SBPortProcessorException {
    SBFileDataFlattenProcessorCallback callback = new SBFileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBFileSizeProcessorCallback());
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> fixOutputMetadata(Map<String, Object> inputs, Map<String, Object> outputs) throws SBPortProcessorException {
    try {
      SBMetadataCallback callback = new SBMetadataCallback(inputs);
      Map<String, Object> fixedOutputs = portProcessor.processOutputs(outputs, callback);
      fixedOutputs = portProcessor.processOutputs(fixedOutputs, callback); // call twice on purpose
      return fixedOutputs;
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to fix metadata", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBLoadContentsPortProcessorCallback());
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBStageInputProcessorCallback(workingDir));
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to stage inputs.", e);
    }
  }

}
