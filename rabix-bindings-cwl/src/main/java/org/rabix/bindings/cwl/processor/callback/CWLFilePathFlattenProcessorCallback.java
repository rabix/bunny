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

class CWLFilePathFlattenProcessorCallback implements CWLPortProcessorCallback {

  private Set<String> flattenedPaths;

  protected CWLFilePathFlattenProcessorCallback() {
    this.flattenedPaths = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      flattenedPaths.add(CWLFileValueHelper.getPath(valueMap).trim());

      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(valueMap);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedPaths.add(CWLFileValueHelper.getPath(secondaryFileValue).trim());
        }
      }
      return new CWLPortProcessorResult(value, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

  public Set<String> getFlattenedPaths() {
    return flattenedPaths;
  }
}
