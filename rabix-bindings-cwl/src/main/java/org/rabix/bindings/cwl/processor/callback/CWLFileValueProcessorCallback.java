package org.rabix.bindings.cwl.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class CWLFileValueProcessorCallback implements CWLPortProcessorCallback {

  private final CWLJob job;
  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;
  private final boolean generateSecondaryFilePaths;

  protected CWLFileValueProcessorCallback(CWLJob job, Set<String> visiblePorts, boolean generateSecondaryFilePaths) {
    this.job = job;
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
    this.generateSecondaryFilePaths = generateSecondaryFilePaths;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = CWLFileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(CWLFileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      } else {
        // try to create secondary files
        if (generateSecondaryFilePaths) {
          Object binding = null;
          if (port instanceof CWLInputPort) {
            binding = ((CWLInputPort) port).getInputBinding();
          } else {
            binding = ((CWLOutputPort) port).getOutputBinding();
          }
          List<String> secondaryFileSufixes = (List<String>) CWLBindingHelper.getSecondaryFiles(binding); // TODO check if it's safe
          if (secondaryFileSufixes != null) {
            List<FileValue> secondaryFileValues = new ArrayList<>();
            for (String suffix : secondaryFileSufixes) {
              String secondaryFilePath = CWLFileValueHelper.getPath(value);

              if (CWLExpressionResolver.isExpressionObject(suffix)) {
                secondaryFilePath = CWLExpressionResolver.resolve(suffix, job, value);
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
      return new CWLPortProcessorResult(value, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(CWLSchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
