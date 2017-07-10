package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileLiteralProcessorCallback implements CWLPortProcessorCallback {

  private File workingDir;
  
  public CWLFileLiteralProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }
  
  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value)) {
      String path = CWLFileValueHelper.getPath(value);
      if (path == null) {
        String contents = CWLFileValueHelper.getContents(value);
        if (StringUtils.isEmpty(contents)) {
          throw new CWLPortProcessorException("Cannot process file literal for port " + id);
        }
        String name = CWLFileValueHelper.getName(value);
        if (StringUtils.isEmpty(name)) {
          name = "file_literal_" + UUID.randomUUID().toString();          
        }
        File file = new File(workingDir, name);
        FileUtils.writeStringToFile(file, contents);
        
        CWLFileValueHelper.setName(name, value);
        CWLFileValueHelper.setDirname(file.getParentFile().getName(), value);
        CWLFileValueHelper.setPath(file.getAbsolutePath(), value);
        return new CWLPortProcessorResult(value, true);
      }
    }
    return new CWLPortProcessorResult(value, false);
  }

}
