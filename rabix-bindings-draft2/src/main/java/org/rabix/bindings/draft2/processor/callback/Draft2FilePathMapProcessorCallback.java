package org.rabix.bindings.draft2.processor.callback;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft2FilePathMapProcessorCallback implements Draft2PortProcessorCallback {

  private final FilePathMapper filePathMapper;
  private final Map<String, Object> config;

  public Draft2FilePathMapProcessorCallback(FilePathMapper filePathMapper, Map<String, Object> config) {
    this.config = config;
    this.filePathMapper = filePathMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Draft2PortProcessorException {
    if (value == null) {
      return new Draft2PortProcessorResult(value, false);
    }
    try {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      if (Draft2SchemaHelper.isFileFromValue(clonedValue)) {
        Map<String, Object> valueMap = (Map<String, Object>) clonedValue;
        String path = Draft2FileValueHelper.getPath(valueMap);

        if (path != null && filePathMapper != null) {
          Draft2FileValueHelper.setPath(filePathMapper.map(path, config), valueMap);

          List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(valueMap);

          if (secondaryFiles != null) {
            for (Map<String, Object> secondaryFile : secondaryFiles) {
              String secondaryFilePath = Draft2FileValueHelper.getPath(secondaryFile);
              Draft2FileValueHelper.setPath(filePathMapper.map(secondaryFilePath, config), secondaryFile);
            }
          }
          return new Draft2PortProcessorResult(valueMap, true);
        }
      }
      return new Draft2PortProcessorResult(clonedValue, false);
    } catch (Exception e) {
      throw new Draft2PortProcessorException(e);
    }
    
  }

}
