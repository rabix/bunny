package org.rabix.bindings.cwl.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileDataFlattenProcessorCallback implements CWLPortProcessorCallback {

  private final Set<Map<String, Object>> flattenedFileData;

  protected CWLFileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value)) {
      flattenedFileData.add((Map<String, Object>) value);

      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFileData.add(secondaryFileValue);
        }
      }
      return new CWLPortProcessorResult(value, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

  public Set<Map<String, Object>> getFlattenedFileData() {
    return flattenedFileData;
  }

}
