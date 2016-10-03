package org.rabix.bindings.cwl.processor.callback;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileDataFlattenProcessorCallback implements CWLPortProcessorCallback {

  private final Set<Object> flattenedFileData;

  protected CWLFileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      flattenedFileData.addAll(flattenSingleFile(value));
      return new CWLPortProcessorResult(value, true);
    }
    return new CWLPortProcessorResult(value, false);
  }
  
  private Set<Object> flattenSingleFile(Object value) {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Set<Object> flattenedFiles = new HashSet<>();
      flattenedFiles.add(value);
      
      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFiles.add(secondaryFileValue);
        }
      }
      
      if (CWLSchemaHelper.isDirectoryFromValue(value)) {
        List<Object> listingObjs = CWLDirectoryValueHelper.getListing(value);
        
        if (listingObjs != null) {
          for (Object listingObj : listingObjs) {
            flattenedFiles.addAll(flattenSingleFile(listingObj));
          }
        }
      }
      return flattenedFiles;
    }
    return Collections.emptySet();
  }

  public Set<Object> getFlattenedFileData() {
    return flattenedFileData;
  }

}
