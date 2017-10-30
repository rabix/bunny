package org.rabix.bindings.cwl.processor.callback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileLiteralProcessorCallback implements CWLPortProcessorCallback {

  private Path workingDir;
  
  public CWLFileLiteralProcessorCallback(Path workingDir) {
    this.workingDir = workingDir;
  }
  
  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (CWLSchemaHelper.isFileFromValue(value)) {
      String path = CWLFileValueHelper.getPath(value);
      String contents = CWLFileValueHelper.getContents(value);
      if (path == null && contents!=null) {
        if (StringUtils.isEmpty(contents)) {
          throw new CWLPortProcessorException("Cannot process file literal for port " + id);
        }
        String name = CWLFileValueHelper.getName(value);
        if (StringUtils.isEmpty(name)) {
          name = "file_literal_" + UUID.randomUUID().toString();          
        }
        Path file = workingDir.resolve(name);
        Files.createDirectories(file.getParent());
        Files.write(file,contents.getBytes());
        
        CWLFileValueHelper.setName(name, value);
        CWLFileValueHelper.setDirname(file.getParent().getFileName().toString(), value);
        CWLFileValueHelper.setPath(file.toString(), value);
        CWLFileValueHelper.setLocation(file.toUri().toString(), value);
        return new CWLPortProcessorResult(value, true);
      }
    }
    return new CWLPortProcessorResult(value, false);
  }

}
