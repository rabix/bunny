package org.rabix.bindings.cwl.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.CloneHelper;

public class CWLFileValueUpdateProcessorCallback implements CWLPortProcessorCallback {

  private FileTransformer fileTransformer;

  public CWLFileValueUpdateProcessorCallback(FileTransformer fileTransformer) {
    this.fileTransformer = fileTransformer;
  }

  @Override
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = fileTransformer.transform(CWLFileValueHelper.createFileValue(clonedValue));
      clonedValue = CWLFileValueHelper.createFileRaw(fileValue);

      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(CWLFileValueHelper.createFileRaw(secondaryFileValue));
        }
        CWLFileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new CWLPortProcessorResult(clonedValue, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

}
