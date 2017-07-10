package org.rabix.bindings.sb.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class SBFileSizeProcessorCallback implements SBPortProcessorCallback {

  @Override
  public SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = SBFileValueHelper.getPath(clonedValue);
      SBFileValueHelper.setSize(new File(path).length(), clonedValue);

      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = SBFileValueHelper.getPath(secondaryFileValue);
          SBFileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new SBPortProcessorResult(clonedValue, true);
    }
    return new SBPortProcessorResult(value, false);
  }

}
