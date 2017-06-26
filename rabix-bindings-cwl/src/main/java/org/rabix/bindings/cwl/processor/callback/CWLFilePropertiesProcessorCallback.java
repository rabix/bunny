package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.CWLProcessor;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWLFilePropertiesProcessorCallback implements CWLPortProcessorCallback {

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = CWLFileValueHelper.getPath(clonedValue);
      File file = new File(path);
      CWLFileValueHelper.setSize(file.length(), clonedValue);
      CWLFileValueHelper.setName(file.getName(), clonedValue);
      
      int dotIndex = file.getName().lastIndexOf(".");
      if (dotIndex != -1) {
        CWLFileValueHelper.setNameext(file.getName().substring(dotIndex), clonedValue);
        CWLFileValueHelper.setNameroot(file.getName().substring(0, dotIndex), clonedValue);
      }
      
      CWLFileValueHelper.setDirname(file.getParentFile().getAbsolutePath(), clonedValue);

      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = CWLFileValueHelper.getPath(secondaryFileValue);
          CWLFileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      
      if (CWLSchemaHelper.isDirectoryFromValue(clonedValue)) {
        List<Object> listing = new ArrayList<>();
        File[] list = file.listFiles();
        if(list != null) {
          for (File childFile : file.listFiles()) {
            listing.add(formFileValue(childFile));
          }
        }
        CWLDirectoryValueHelper.setListing(listing, clonedValue);
      }
      
      return new CWLPortProcessorResult(clonedValue, true);
    }
    return new CWLPortProcessorResult(value, false);
  }
  
  public Map<String, Object> formFileValue(File file) throws CWLExpressionException, IOException {
    if (file.isDirectory()) {
      Map<String, Object> directory = new HashMap<>();
      CWLDirectoryValueHelper.setDirectoryType(directory);
      CWLDirectoryValueHelper.setSize(file.length(), directory);
      CWLDirectoryValueHelper.setName(file.getName(), directory);
      CWLDirectoryValueHelper.setPath(file.getAbsolutePath(), directory);
      
      File[] list = file.listFiles();
      
      List<Object> listing = new ArrayList<>();
      for (File subfile : list) {
        switch (subfile.getName()) {
        case CWLProcessor.JOB_FILE:
        case CWLProcessor.RESULT_FILENAME:
        case CWLProcessor.RESERVED_EXECUTOR_CMD_LOG_FILE_NAME:
        case CWLProcessor.RESERVED_EXECUTOR_ERROR_LOG_FILE_NAME:
          continue;
        default:
          break;
        }
        listing.add(formFileValue(subfile));
      }
      CWLDirectoryValueHelper.setListing(listing, directory);
      return directory;
    }

    Map<String, Object> fileData = new HashMap<>();
    CWLFileValueHelper.setFileType(fileData);
    CWLFileValueHelper.setSize(file.length(), fileData);
    CWLFileValueHelper.setName(file.getName(), fileData);
    CWLFileValueHelper.setDirname(file.getParentFile().getAbsolutePath(), fileData);
    CWLFileValueHelper.setPath(file.getAbsolutePath(), fileData);
    return fileData;
  }

}
