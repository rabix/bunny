package org.rabix.bindings.draft3.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft3FileValueProcessorCallback implements Draft3PortProcessorCallback {

  private final Draft3Job job;
  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;
  private final boolean generateSecondaryFilePaths;

  protected Draft3FileValueProcessorCallback(Draft3Job job, Set<String> visiblePorts, boolean generateSecondaryFilePaths) {
    this.job = job;
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
    this.generateSecondaryFilePaths = generateSecondaryFilePaths;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = Draft3FileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(Draft3FileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      } else {
        // try to create secondary files
        if (generateSecondaryFilePaths) {
          Object binding = null;
          if (port instanceof Draft3InputPort) {
            binding = ((Draft3InputPort) port).getInputBinding();
          } else {
            binding = ((Draft3OutputPort) port).getOutputBinding();
          }
          List<String> secondaryFileSufixes = (List<String>) Draft3BindingHelper.getSecondaryFiles(binding); // TODO check if it's safe
          if (secondaryFileSufixes != null) {
            List<FileValue> secondaryFileValues = new ArrayList<>();
            for (String suffix : secondaryFileSufixes) {
              String secondaryFilePath = Draft3FileValueHelper.getPath(value);

              if (Draft3ExpressionResolver.isExpressionObject(suffix)) {
                secondaryFilePath = Draft3ExpressionResolver.resolve(suffix, job, value);
              } else {
                while (suffix.startsWith("^")) {
                  int extensionIndex = secondaryFilePath.lastIndexOf(".");
                  if (extensionIndex != -1) {
                    secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
                    suffix = suffix.substring(1);
                  } else {
                    break;
                  }
                }
                secondaryFilePath += suffix.startsWith(".") ? suffix : "." + suffix;
              }
              secondaryFileValues.add(new FileValue(null, secondaryFilePath, null, null, null));
            }
            fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
          }
        }
      }
      fileValues.add(fileValue);
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(Draft3SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
