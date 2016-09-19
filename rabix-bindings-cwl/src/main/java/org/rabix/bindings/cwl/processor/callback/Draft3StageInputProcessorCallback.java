package org.rabix.bindings.draft3.processor.callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3InputPort.StageInput;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft3StageInputProcessorCallback implements Draft3PortProcessorCallback {

  private static final Logger logger = LoggerFactory.getLogger(Draft3StageInputProcessorCallback.class);

  private final File workingDir;

  public Draft3StageInputProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (!(port instanceof Draft3InputPort)) {
      throw new RuntimeException("Inputs only can be staged!");
    }
    Draft3InputPort inputPort = (Draft3InputPort) port;
    String stageInputStr = inputPort.getStageInput();
    if (stageInputStr == null) {
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(stage(value, StageInput.get(stageInputStr)), true);
  }

  @SuppressWarnings("unchecked")
  public Object stage(Object value, StageInput stageInput) throws BindingException {
    if (value == null) {
      return null;
    }
    if (Draft3SchemaHelper.isFileFromValue(value)) {
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
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      String originalPath = Draft3FileValueHelper.getPath(value);
      String path = stagePath(originalPath, stageInput);
      Draft3FileValueHelper.setPath(path, value);
      Draft3FileValueHelper.setOriginalPath(originalPath, value);;

      List<Map<String, Object>> secondaryFileValues = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFileValues != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
          String secondaryFilePath = stagePath(Draft3FileValueHelper.getPath(secondaryFileValue), stageInput);
          Draft3FileValueHelper.setPath(secondaryFilePath, secondaryFileValue);
        }
      }
    }
    return value;
  }

  private String stagePath(String path, StageInput stageInput) throws BindingException {
    switch (stageInput) { // just copy for now
    case COPY:
    case LINK:
      File file = new File(path);
      if (!file.exists()) {
        throw new BindingException("Failed to stage input file path " + path);
      }
      File destinationFile = new File(workingDir, file.getName());
      if (destinationFile.exists()) {
        throw new BindingException("Failed to stage input file path " + path + ". File with the same name already exists.");
      }
      logger.info("Stage input file {} to {}.", file, destinationFile);
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
    default:
      throw new BindingException("Failed to stage input files. StageInput " + stageInput + " is not supported");
    }
  }
}
