package org.rabix.bindings.sb.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.SBProcessor;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.CloneHelper;

public class SBInputSecondaryFilesProcessor implements SBPortProcessorCallback {

  private SBJob job;
  private File workingDir;
  private HashAlgorithm hashAlgorithm;

  public SBInputSecondaryFilesProcessor(SBJob job, HashAlgorithm hashAlgorithm, File workingDir) {
    this.job = job;
    this.workingDir = workingDir;
    this.hashAlgorithm = hashAlgorithm;
  }

  @Override
  @SuppressWarnings("unchecked")
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value) && port instanceof SBInputPort) {
      if (!CollectionUtils.isEmpty(SBFileValueHelper.getSecondaryFiles(value))) {
        return new SBPortProcessorResult(value, false); 
      }
      Object secondaryFilesObj = ((SBInputPort) port).getInputBinding();
      if (secondaryFilesObj == null) {
        return new SBPortProcessorResult(value, false);
      }

      Map<String, Object> clonedValue = (Map<String, Object>) CloneHelper.deepCopy(value);
      List<Map<String, Object>> secondaryFiles = SBProcessor.getSecondaryFiles(job, hashAlgorithm, clonedValue,  SBFileValueHelper.getLocation(clonedValue), secondaryFilesObj);
      if (secondaryFiles != null) {
        SBFileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
        return new SBPortProcessorResult(clonedValue, true);
      }
    }
    return new SBPortProcessorResult(value, false);
  }
}