package org.rabix.bindings.draft3.processor.callback;

import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft3LoadContentsPortProcessorCallback implements Draft3PortProcessorCallback {

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value) && port instanceof Draft3InputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((Draft3InputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new Draft3PortProcessorResult(clonedValue, true);
      }

      boolean loadContents = Draft3BindingHelper.loadContents(inputBinding);
      if (loadContents) {
        Draft3FileValueHelper.setContents(clonedValue);
        return new Draft3PortProcessorResult(clonedValue, true);
      }
    }
    return new Draft3PortProcessorResult(value, false);
  }

}
