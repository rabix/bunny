package org.rabix.bindings.draft2.processor.callback;

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
import org.rabix.bindings.draft2.bean.Draft2InputPort;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.StageInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft2StageInputProcessorCallback implements Draft2PortProcessorCallback {

  private static final Logger logger = LoggerFactory.getLogger(Draft2StageInputProcessorCallback.class);

  private final File workingDir;

  public Draft2StageInputProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (!(port instanceof Draft2InputPort)) {
      throw new RuntimeException("Inputs only can be staged!");
    }
    Draft2InputPort inputPort = (Draft2InputPort) port;
    StageInput stageInput = inputPort.getStageInput();
    if (stageInput == null) {
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(stage(value, stageInput), true);
  }

  @SuppressWarnings("unchecked")
  public Object stage(Object value, StageInput stageInput) throws BindingException {
    if (value == null) {
      return null;
    }
    if (Draft2SchemaHelper.isFileFromValue(value)) {
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
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      String originalPath = Draft2FileValueHelper.getPath(value);
      String path = stagePath(originalPath, stageInput);
      Draft2FileValueHelper.setPath(path, value);
      Draft2FileValueHelper.setOriginalPath(originalPath, value);;

      List<Map<String, Object>> secondaryFileValues = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFileValues != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
          String secondaryFilePath = stagePath(Draft2FileValueHelper.getPath(secondaryFileValue), stageInput);
          Draft2FileValueHelper.setPath(secondaryFilePath, secondaryFileValue);
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
