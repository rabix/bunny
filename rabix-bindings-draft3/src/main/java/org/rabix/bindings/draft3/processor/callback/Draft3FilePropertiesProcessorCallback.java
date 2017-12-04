package org.rabix.bindings.draft3.processor.callback;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft3FilePropertiesProcessorCallback implements Draft3PortProcessorCallback {
  private Path appFile;

  public Draft3FilePropertiesProcessorCallback(Path appFile) {
    this.appFile = appFile;
  }

  public Draft3FilePropertiesProcessorCallback(String appFile) {
    this.appFile = Paths.get(appFile);
  }

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      Draft3FileValueHelper.buildMissingInfo(clonedValue, null, appFile.getParent());
      return new Draft3PortProcessorResult(clonedValue, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }
}
