package org.rabix.bindings.sb.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.CloneHelper;

public class SBFileValueUpdateProcessorCallback implements SBPortProcessorCallback {

  private FileTransformer fileTransformer;

  public SBFileValueUpdateProcessorCallback(FileTransformer fileTransformer) {
    this.fileTransformer = fileTransformer;
  }

  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = fileTransformer.transform(SBFileValueHelper.createFileValue(clonedValue));
      clonedValue = SBFileValueHelper.createFileRaw(fileValue);

      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(SBFileValueHelper.createFileRaw(secondaryFileValue));
        }
        SBFileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new SBPortProcessorResult(clonedValue, true);
    }
    return new SBPortProcessorResult(value, false);
  }

}
