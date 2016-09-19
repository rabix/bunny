package org.rabix.bindings.draft3.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.CloneHelper;

public class Draft3FileValueUpdateProcessorCallback implements Draft3PortProcessorCallback {

  private FileTransformer fileTransformer;

  public Draft3FileValueUpdateProcessorCallback(FileTransformer fileTransformer) {
    this.fileTransformer = fileTransformer;
  }

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = fileTransformer.transform(Draft3FileValueHelper.createFileValue(clonedValue));
      clonedValue = Draft3FileValueHelper.createFileRaw(fileValue);

      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(Draft3FileValueHelper.createFileRaw(secondaryFileValue));
        }
        Draft3FileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new Draft3PortProcessorResult(clonedValue, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }

}
