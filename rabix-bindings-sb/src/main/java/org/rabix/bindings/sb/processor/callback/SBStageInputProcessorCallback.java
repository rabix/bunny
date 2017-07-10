package org.rabix.bindings.sb.processor.callback;

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
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.StageInput;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SBStageInputProcessorCallback implements SBPortProcessorCallback {

  private static final Logger logger = LoggerFactory.getLogger(SBStageInputProcessorCallback.class);

  private final File workingDir;

  public SBStageInputProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (!(parentPort instanceof SBInputPort)) {
      throw new RuntimeException("Inputs only can be staged!");
    }
    SBInputPort inputPort = (SBInputPort) parentPort;
    StageInput stageInput = inputPort.getStageInput();
    if (stageInput == null) {
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(stage(value, stageInput), true);
  }

  @SuppressWarnings("unchecked")
  public Object stage(Object value, StageInput stageInput) throws BindingException {
    if (value == null) {
      return null;
    }
    if (SBSchemaHelper.isFileFromValue(value)) {
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
    if (SBSchemaHelper.isFileFromValue(value)) {
      String originalPath = SBFileValueHelper.getPath(value);
      String path = stagePath(originalPath, stageInput);
      SBFileValueHelper.setPath(path, value);
      SBFileValueHelper.setOriginalPath(originalPath, value);;

      List<Map<String, Object>> secondaryFileValues = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFileValues != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
          String secondaryFilePath = stagePath(SBFileValueHelper.getPath(secondaryFileValue), stageInput);
          SBFileValueHelper.setPath(secondaryFilePath, secondaryFileValue);
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
