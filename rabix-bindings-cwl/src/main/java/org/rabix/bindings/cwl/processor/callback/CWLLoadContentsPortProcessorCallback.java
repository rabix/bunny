package org.rabix.bindings.cwl.processor.callback;

import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLLoadContentsPortProcessorCallback implements CWLPortProcessorCallback {

  @Override
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if ((CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) && port instanceof CWLInputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((CWLInputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new CWLPortProcessorResult(clonedValue, true);
      }

      boolean loadContents = CWLBindingHelper.loadContents(inputBinding);
      if (loadContents) {
        CWLFileValueHelper.setContents(clonedValue);
        return new CWLPortProcessorResult(clonedValue, true);
      }
    }
    return new CWLPortProcessorResult(value, false);
  }

}
