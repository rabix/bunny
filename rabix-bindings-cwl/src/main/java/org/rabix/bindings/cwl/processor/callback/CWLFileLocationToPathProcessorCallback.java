package org.rabix.bindings.cwl.processor.callback;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;

public class CWLFileLocationToPathProcessorCallback implements CWLPortProcessorCallback {

  private Path appFile;

  public CWLFileLocationToPathProcessorCallback(Path appFile) {
    this.appFile = appFile;
  }

  public CWLFileLocationToPathProcessorCallback(String appFile) {
    this.appFile = Paths.get(appFile);
  }

  @Override
  public CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws CWLPortProcessorException {
    if (value == null) {
      return new CWLPortProcessorResult(value, false);
    }
    try {
      if (CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) {
        setPaths(value);
        CWLFileValueHelper.getSecondaryFiles(value).stream().forEach(f -> setPaths(f));
        if (CWLSchemaHelper.isDirectoryFromValue(value)) {
          CWLDirectoryValueHelper.getListing(value).stream().forEach(f -> setPaths(f));
        }
        return new CWLPortProcessorResult(value, true);
      }
      return new CWLPortProcessorResult(value, false);
    } catch (Exception e) {
      throw new CWLPortProcessorException(e);
    }
  }

  private void setPaths(Object value) {
    String path = CWLFileValueHelper.getPath(value);
    String location = CWLFileValueHelper.getLocation(value);

    if (path == null && location != null) {
      Path path2 = Paths.get(location);
      if (!path2.isAbsolute()) {
        path2 = appFile.resolveSibling(path2);
        CWLFileValueHelper.setLocation(path2.toUri().toString(), value);

      }
      CWLFileValueHelper.setPath(path2.toString(), value);
    }
    if(path != null && location==null){
      Path path2 = Paths.get(path);
      CWLFileValueHelper.setLocation(path2.toUri().toString(), value);
    }
//    String name = CWLFileValueHelper.getName(value);
//    if (name != null && !path.endsWith(name)) {
//      CWLFileValueHelper.setPath(path.substring(0, path.lastIndexOf("/") + 1) + name, value);
//    }
  }

}
