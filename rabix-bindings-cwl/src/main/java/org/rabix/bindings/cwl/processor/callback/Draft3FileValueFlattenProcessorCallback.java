package org.rabix.bindings.draft3.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft3FileValueFlattenProcessorCallback implements Draft3PortProcessorCallback {

  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;

  protected Draft3FileValueFlattenProcessorCallback(Set<String> visiblePorts) {
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
  }

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      fileValues.add(Draft3FileValueHelper.createFileValue(value));
      
      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          fileValues.add(Draft3FileValueHelper.createFileValue(secondaryFileValue));
        }
      }
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }
  
  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(Draft3SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
