package org.rabix.bindings.cwl.processor.callback;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLFilePathMapProcessorCallback implements CWLPortProcessorCallback {

  private final FilePathMapper filePathMapper;
  private final Map<String, Object> config;

  public CWLFilePathMapProcessorCallback(FilePathMapper filePathMapper, Map<String, Object> config) {
    this.config = config;
    this.filePathMapper = filePathMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws CWLPortProcessorException {
    if (value == null) {
      return new CWLPortProcessorResult(value, false);
    }
    try {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      if (CWLSchemaHelper.isFileFromValue(clonedValue)) {
        Map<String, Object> valueMap = (Map<String, Object>) clonedValue;
        String path = CWLFileValueHelper.getPath(valueMap);

        if (path != null && filePathMapper != null) {
          CWLFileValueHelper.setPath(filePathMapper.map(path, config), valueMap);

          List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(valueMap);

          if (secondaryFiles != null) {
            for (Map<String, Object> secondaryFile : secondaryFiles) {
              String secondaryFilePath = CWLFileValueHelper.getPath(secondaryFile);
              CWLFileValueHelper.setPath(filePathMapper.map(secondaryFilePath, config), secondaryFile);
            }
          }
          return new CWLPortProcessorResult(valueMap, true);
        }
      }
      return new CWLPortProcessorResult(clonedValue, false);
    } catch (Exception e) {
      throw new CWLPortProcessorException(e);
    }
    
  }

}
