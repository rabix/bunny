package org.rabix.bindings.draft3.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft3FileSizeProcessorCallback implements Draft3PortProcessorCallback {

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = Draft3FileValueHelper.getPath(clonedValue);
      Draft3FileValueHelper.setSize(new File(path).length(), clonedValue);

      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = Draft3FileValueHelper.getPath(secondaryFileValue);
          Draft3FileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new Draft3PortProcessorResult(clonedValue, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }

}
