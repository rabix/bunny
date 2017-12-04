package org.rabix.bindings.sb.processor.callback;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class SBFilePropertiesProcessorCallback implements SBPortProcessorCallback {
  private Path appFile;

  public SBFilePropertiesProcessorCallback(Path appFile) {
    this.appFile = appFile;
  }

  public SBFilePropertiesProcessorCallback(String appFile) {
    this.appFile = Paths.get(appFile);
  }

  @Override
  public SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      SBFileValueHelper.buildMissingInfo(clonedValue, null, appFile.getParent());
      return new SBPortProcessorResult(clonedValue, true);
    }
    return new SBPortProcessorResult(value, false);
  }
}
