package org.rabix.bindings.cwl.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.CWLJobProcessor;
import org.rabix.bindings.cwl.CWLValueTranslator;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class CWLJobHelper {

  @SuppressWarnings("unchecked")
  public static CWLJob getCWLJob(Job job) throws BindingException {
    String resolvedAppStr = CWLDocumentResolver.resolve(job.getApp());
    CWLJobApp app = BeanSerializer.deserialize(resolvedAppStr, CWLJobApp.class);

    Map<String, Object> nativeInputs = (Map<String, Object>) CWLValueTranslator.translateToSpecific(job.getInputs());
    Map<String, Object> nativeOutputs = (Map<String, Object>) CWLValueTranslator.translateToSpecific(job.getOutputs());
    
    CWLJob cwlJob =  new CWLJobProcessor().process(new CWLJob(job.getName(), app, nativeInputs, nativeOutputs));
    
    CWLRuntime runtime = null;
    try {
      runtime = CWLRuntimeHelper.createRuntime(cwlJob, job.getResources());
      cwlJob.setRuntime(runtime);
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
    return cwlJob;
  }
  
}
