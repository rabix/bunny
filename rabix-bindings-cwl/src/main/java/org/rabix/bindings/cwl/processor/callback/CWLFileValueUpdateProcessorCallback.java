package org.rabix.bindings.cwl.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.CloneHelper;

public class CWLFileValueUpdateProcessorCallback implements CWLPortProcessorCallback {

  private FileTransformer fileTransformer;

  public CWLFileValueUpdateProcessorCallback(FileTransformer fileTransformer) {
    this.fileTransformer = fileTransformer;
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = null;
      if (CWLSchemaHelper.isFileFromValue(value)) {
        fileValue = fileTransformer.transform(CWLFileValueHelper.createFileValue(clonedValue));
        clonedValue = CWLFileValueHelper.createFileRaw(fileValue);
      } else {
        DirectoryValue directoryValue = (DirectoryValue) fileTransformer.transform(CWLDirectoryValueHelper.createDirectoryValue(clonedValue));
        clonedValue = CWLDirectoryValueHelper.createDirectoryRaw(directoryValue);
        fileValue = directoryValue;
      }

      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          if (CWLSchemaHelper.isFileFromValue(value)) {
            secondaryFiles.add(CWLFileValueHelper.createFileRaw(secondaryFileValue));
          } else {
            secondaryFiles.add(CWLDirectoryValueHelper.createDirectoryRaw((DirectoryValue) secondaryFileValue));
          }
        }
        CWLFileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new CWLPortProcessorResult(clonedValue, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

}
