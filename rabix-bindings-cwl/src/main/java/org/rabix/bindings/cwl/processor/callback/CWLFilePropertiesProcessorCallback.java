package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLFilePropertiesProcessorCallback implements CWLPortProcessorCallback {

  @Override
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = CWLFileValueHelper.getPath(clonedValue);
      File file = new File(path);
      CWLFileValueHelper.setSize(file.length(), clonedValue);
      CWLFileValueHelper.setName(file.getName(), clonedValue);
      CWLFileValueHelper.setDirname(file.getParentFile().getAbsolutePath(), clonedValue);

      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = CWLFileValueHelper.getPath(secondaryFileValue);
          CWLFileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new CWLPortProcessorResult(clonedValue, true);
    }
    return new CWLPortProcessorResult(value, false);
  }

}
