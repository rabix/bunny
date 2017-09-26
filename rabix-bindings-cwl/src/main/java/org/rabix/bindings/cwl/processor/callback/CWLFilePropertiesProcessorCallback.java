package org.rabix.bindings.cwl.processor.callback;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLFilePropertiesProcessorCallback implements CWLPortProcessorCallback {
  private Path appFile;

  public CWLFilePropertiesProcessorCallback(Path appFile) {
    this.appFile = appFile;
  }

  public CWLFilePropertiesProcessorCallback(String appFile) {
    this.appFile = Paths.get(appFile);
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      CWLFileValueHelper.buildMissingInfo(clonedValue, null, appFile.getParent());
      return new CWLPortProcessorResult(clonedValue, true);
    }
    return new CWLPortProcessorResult(value, false);
  }
}
