package org.rabix.bindings.cwl.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.CWLProcessor;
import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.CloneHelper;

public class CWLInputSecondaryFilesProcessor implements CWLPortProcessorCallback {

  private CWLJob job;
  private File workingDir;
  private HashAlgorithm hashAlgorithm;

  public CWLInputSecondaryFilesProcessor(CWLJob job, HashAlgorithm hashAlgorithm, File workingDir) {
    this.job = job;
    this.workingDir = workingDir;
    this.hashAlgorithm = hashAlgorithm;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if ((CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value)) && port instanceof CWLInputPort) {
      if (CWLFileValueHelper.getSecondaryFiles(value) != null) {
        return new CWLPortProcessorResult(value, false); 
      }
      Object secondaryFilesObj = ((CWLInputPort) port).getSecondaryFiles();
      if (secondaryFilesObj == null) {
        return new CWLPortProcessorResult(value, false);
      }

      Map<String, Object> clonedValue = (Map<String, Object>) CloneHelper.deepCopy(value);
      List<Map<String, Object>> secondaryFiles = CWLProcessor.getSecondaryFiles(job, hashAlgorithm, clonedValue,  CWLFileValueHelper.getLocation(clonedValue), secondaryFilesObj, workingDir);
      if (secondaryFiles != null) {
        CWLFileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
        return new CWLPortProcessorResult(clonedValue, true);
      }
    }
    return new CWLPortProcessorResult(value, false);
  }
}