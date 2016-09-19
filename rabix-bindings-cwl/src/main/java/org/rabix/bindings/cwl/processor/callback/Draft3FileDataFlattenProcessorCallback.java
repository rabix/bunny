package org.rabix.bindings.draft3.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class Draft3FileDataFlattenProcessorCallback implements Draft3PortProcessorCallback {

  private final Set<Map<String, Object>> flattenedFileData;

  protected Draft3FileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      flattenedFileData.add((Map<String, Object>) value);

      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFileData.add(secondaryFileValue);
        }
      }
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }

  public Set<Map<String, Object>> getFlattenedFileData() {
    return flattenedFileData;
  }

}
