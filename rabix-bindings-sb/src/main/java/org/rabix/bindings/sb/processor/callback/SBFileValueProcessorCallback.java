package org.rabix.bindings.sb.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBOutputPort;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;
import org.rabix.bindings.sb.helper.SBBindingHelper;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;

public class SBFileValueProcessorCallback implements SBPortProcessorCallback {

  private final SBJob job;
  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;
  private final boolean generateSecondaryFilePaths;

  protected SBFileValueProcessorCallback(SBJob job, Set<String> visiblePorts, boolean generateSecondaryFilePaths) {
    this.job = job;
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
    this.generateSecondaryFilePaths = generateSecondaryFilePaths;
  }
  
  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = SBFileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(SBFileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      } else {
        // try to create secondary files
        if (generateSecondaryFilePaths) {
          Object binding = null;
          if (port instanceof SBInputPort) {
            binding = ((SBInputPort) port).getInputBinding();
          } else {
            binding = ((SBOutputPort) port).getOutputBinding();
          }
          List<String> secondaryFileSufixes = SBBindingHelper.getSecondaryFiles(binding);
          if (secondaryFileSufixes != null) {

            List<FileValue> secondaryFileValues = new ArrayList<>();
            for (String suffix : secondaryFileSufixes) {
              String secondaryFilePath = SBFileValueHelper.getPath(value);

              if (SBExpressionBeanHelper.isExpression(suffix)) {
                secondaryFilePath = SBExpressionBeanHelper.evaluate(job, value, suffix);
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
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(SBSchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
