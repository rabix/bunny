package org.rabix.bindings.draft2.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft2.bean.Draft2InputPort;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2OutputPort;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.draft2.helper.Draft2BindingHelper;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft2FileValueProcessorCallback implements Draft2PortProcessorCallback {

  private final Draft2Job job;
  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;
  private final boolean generateSecondaryFilePaths;

  protected Draft2FileValueProcessorCallback(Draft2Job job, Set<String> visiblePorts, boolean generateSecondaryFilePaths) {
    this.job = job;
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
    this.generateSecondaryFilePaths = generateSecondaryFilePaths;
  }
  
  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = Draft2FileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(Draft2FileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      } else {
        // try to create secondary files
        if (generateSecondaryFilePaths) {
          Object binding = null;
          if (port instanceof Draft2InputPort) {
            binding = ((Draft2InputPort) port).getInputBinding();
          } else {
            binding = ((Draft2OutputPort) port).getOutputBinding();
          }
          List<String> secondaryFileSufixes = Draft2BindingHelper.getSecondaryFiles(binding);
          if (secondaryFileSufixes != null) {

            List<FileValue> secondaryFileValues = new ArrayList<>();
            for (String suffix : secondaryFileSufixes) {
              String secondaryFilePath = Draft2FileValueHelper.getPath(value);

              if (Draft2ExpressionBeanHelper.isExpression(suffix)) {
                secondaryFilePath = Draft2ExpressionBeanHelper.evaluate(job, value, suffix);
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
              secondaryFileValues.add(new FileValue(null, secondaryFilePath, null, null, null, null));
            }
            fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
          }
        }
      }
      fileValues.add(fileValue);
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(Draft2SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
