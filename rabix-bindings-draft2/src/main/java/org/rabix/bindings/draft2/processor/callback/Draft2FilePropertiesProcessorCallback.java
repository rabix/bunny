package org.rabix.bindings.draft2.processor.callback;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft2FilePropertiesProcessorCallback implements Draft2PortProcessorCallback {
  private Path appFile;

  public Draft2FilePropertiesProcessorCallback(Path appFile) {
    this.appFile = appFile;
  }

  public Draft2FilePropertiesProcessorCallback(String appFile) {
    this.appFile = Paths.get(appFile);
  }

  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      Draft2FileValueHelper.buildMissingInfo(clonedValue, null, appFile.getParent());
      return new Draft2PortProcessorResult(clonedValue, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }
}
