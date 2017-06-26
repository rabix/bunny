package org.rabix.bindings.cwl.processor.callback;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileLocationToPathProcessorCallback implements CWLPortProcessorCallback {

  public CWLFileLocationToPathProcessorCallback() {
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws CWLPortProcessorException {
    if (value == null) {
      return new CWLPortProcessorResult(value, false);
    }
    try {
      if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
        if (CWLFileValueHelper.getPath(value) == null) {
          CWLFileValueHelper.setPath(CWLFileValueHelper.getLocation(value), value);
        }
        return new CWLPortProcessorResult(value, true);
      }
      return new CWLPortProcessorResult(value, false);
    } catch (Exception e) {
      throw new CWLPortProcessorException(e);
    }
  }
  
}
