package org.rabix.bindings.cwl.processor.callback;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileBasenameCheckProcessorCallback implements CWLPortProcessorCallback {

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      String basename = CWLFileValueHelper.getName(value);
      String path = CWLFileValueHelper.getPath(value);
      
      if (!path.endsWith(basename)) {
        
      }
    }
    
    return null;
  }

}
