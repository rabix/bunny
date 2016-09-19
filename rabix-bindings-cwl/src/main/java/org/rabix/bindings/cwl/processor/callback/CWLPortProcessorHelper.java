package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.processor.CWLPortProcessor;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.transformer.FileTransformer;

public class CWLPortProcessorHelper {

  private final CWLJob cwlJob;
  private final CWLPortProcessor portProcessor;

  public CWLPortProcessorHelper(CWLJob cwlJob) {
    this.cwlJob = cwlJob;
    this.portProcessor = new CWLPortProcessor(cwlJob);
  }
  
  public Set<FileValue> getInputFiles(Map<String, Object> inputs, FilePathMapper fileMapper, Map<String, Object> config) throws CWLPortProcessorException {
    if (fileMapper != null) {
      CWLFilePathMapProcessorCallback fileMapperCallback = new CWLFilePathMapProcessorCallback(fileMapper, config);
      inputs = portProcessor.processInputs(inputs, fileMapperCallback);
    }
    
    CWLFileValueProcessorCallback callback = new CWLFileValueProcessorCallback(cwlJob, null, true);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to get input files.", e);
    }
    return callback.getFileValues();
  }
  
  public Set<FileValue> getOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws CWLPortProcessorException {
    CWLFileValueProcessorCallback callback = new CWLFileValueProcessorCallback(cwlJob, visiblePorts, false);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to get output files.", e);
    }
    return callback.getFileValues();
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws CWLPortProcessorException {
    CWLFilePathFlattenProcessorCallback callback = new CWLFilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws CWLPortProcessorException {
    CWLFileValueFlattenProcessorCallback callback = new CWLFileValueFlattenProcessorCallback(null);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws CWLPortProcessorException {
    CWLFileValueFlattenProcessorCallback callback = new CWLFileValueFlattenProcessorCallback(visiblePorts);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws CWLPortProcessorException {
    CWLFilePathFlattenProcessorCallback callback = new CWLFilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Map<String, Object> updateInputFiles(Map<String, Object> inputs, FileTransformer fileTransformer) throws CWLPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWLFileValueUpdateProcessorCallback(fileTransformer));
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> updateOutputFiles(Map<String, Object> outputs, FileTransformer fileTransformer) throws CWLPortProcessorException {
    try {
      return portProcessor.processOutputs(outputs, new CWLFileValueUpdateProcessorCallback(fileTransformer));
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws CWLPortProcessorException {
    CWLFileDataFlattenProcessorCallback callback = new CWLFileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws CWLPortProcessorException {
    CWLFileDataFlattenProcessorCallback callback = new CWLFileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws CWLPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWLFileSizeProcessorCallback());
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to set input file size", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws CWLPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWLLoadContentsPortProcessorCallback());
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws CWLPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWLStageInputProcessorCallback(workingDir));
    } catch (CWLPortProcessorException e) {
      throw new CWLPortProcessorException("Failed to stage inputs.", e);
    }
  }

}
