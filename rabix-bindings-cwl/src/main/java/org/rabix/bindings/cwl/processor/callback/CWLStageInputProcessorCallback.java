package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.StageInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWLStageInputProcessorCallback implements CWLPortProcessorCallback {

  private static final Logger logger = LoggerFactory.getLogger(CWLStageInputProcessorCallback.class);

  private final File workingDir;

  public CWLStageInputProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (!(parentPort instanceof CWLInputPort)) {
      throw new RuntimeException("Inputs only can be staged!");
    }
    CWLInputPort inputPort = (CWLInputPort) parentPort;
    StageInput stageInput = inputPort.getStageInput();
    if (stageInput == null) {
      return new CWLPortProcessorResult(value, true);
    }
    return new CWLPortProcessorResult(stage(value, stageInput), true);
  }

  @SuppressWarnings("unchecked")
  public Object stage(Object value, StageInput stageInput) throws BindingException {
    if (value == null) {
      return null;
    }
    if (CWLSchemaHelper.isFileFromValue(value)) {
      return stageSingle(value, stageInput);
    } else if (value instanceof List<?>) {
      List<Object> stagedValues = new ArrayList<>();
      for (Object subvalue : ((List<?>) value)) {
        stagedValues.add(stage(subvalue, stageInput));
      }
      return stagedValues;
    } else if (value instanceof Map<?, ?>) {
      Map<String, Object> stagedValues = new HashMap<>();
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        stagedValues.put(entry.getKey(), stage(entry.getValue(), stageInput));
      }
      return stagedValues;
    }
    return value;
  }

  private Object stageSingle(Object value, StageInput stageInput) throws BindingException {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      String originalPath = CWLFileValueHelper.getPath(value);
      String path = stagePath(originalPath, stageInput);
      CWLFileValueHelper.setPath(path, value);
      CWLFileValueHelper.setOriginalPath(originalPath, value);;

      List<Map<String, Object>> secondaryFileValues = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFileValues != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
          String secondaryFilePath = stagePath(CWLFileValueHelper.getPath(secondaryFileValue), stageInput);
          CWLFileValueHelper.setPath(secondaryFilePath, secondaryFileValue);
        }
      }
    }
    return value;
  }

  private String stagePath(String path, StageInput stageInput) throws BindingException {
    File file = new File(path);
    if (!file.exists()) {
      throw new BindingException("Failed to stage input file path " + path);
    }
    File destinationFile = new File(workingDir, file.getName());
    if (destinationFile.exists()) {
      throw new BindingException("Failed to stage input file path " + path + ". File with the same name already exists.");
    }
    logger.info("Stage input file {} to {}.", file, destinationFile);
    switch (stageInput) { // just copy for now
    case COPY:
      try {
        if (file.isFile()) {
          FileUtils.copyFile(file, destinationFile);
        } else {
          FileUtils.copyDirectory(file, destinationFile);
        }
      } catch (IOException e) {
        throw new BindingException(e);
      }
      return destinationFile.getAbsolutePath();
    case LINK:
        try {
          Files.createLink(destinationFile.toPath(), file.toPath());
        } catch (IOException e) {
          throw new BindingException(e);
        }
     return destinationFile.getAbsolutePath();
    default:
      throw new BindingException("Failed to stage input files. StageInput " + stageInput + " is not supported");
    }
  }
}
