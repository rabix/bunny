package org.rabix.bindings.cwl.processor.callback;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLFilePathMapProcessorCallback implements CWLPortProcessorCallback {

  private final Map<String, Object> config;
  private final FilePathMapper filePathMapper;
  
  public CWLFilePathMapProcessorCallback(FilePathMapper filePathMapper, Map<String, Object> config) {
    this.config = config;
    this.filePathMapper = filePathMapper;
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws CWLPortProcessorException {
    if (value == null) {
      return new CWLPortProcessorResult(value, false);
    }
    try {
      if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
        return new CWLPortProcessorResult(mapSingleFile(value), true);
      }
      return new CWLPortProcessorResult(value, false);
    } catch (Exception e) {
      throw new CWLPortProcessorException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private Object mapSingleFile(Object value) throws FileMappingException {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      String path = CWLFileValueHelper.getPath(clonedValue);
      if (StringUtils.isEmpty(path)) { // file literals
        return value;
      }

      CWLFileValueHelper.setPath(filePathMapper.map(path, config), clonedValue);
      
      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          CWLFileValueHelper.setPath(filePathMapper.map(CWLFileValueHelper.getPath(secondaryFileValue), config), secondaryFileValue);
        }
      }

      if (CWLSchemaHelper.isDirectoryFromValue(value)) {
        List<Object> listingObjs = CWLDirectoryValueHelper.getListing(value);
        
        List<Object> clonedListingObjs = (List<Object>) CloneHelper.deepCopy(listingObjs);
        if (listingObjs != null) {
          for (Object listingObj : listingObjs) {
            clonedListingObjs.add(mapSingleFile(listingObj));
          }
          CWLDirectoryValueHelper.setListing(clonedListingObjs, value);
        }
      }
      return clonedValue;
    }
    return value;
  }

}
