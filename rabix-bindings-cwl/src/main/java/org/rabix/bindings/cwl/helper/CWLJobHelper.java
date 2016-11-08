package org.rabix.bindings.cwl.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.CWLJobProcessor;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLResourceRequirement;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class CWLJobHelper {

  public static CWLJob getCWLJob(Job job) throws BindingException {
    String resolvedAppStr = CWLDocumentResolver.resolve(job.getApp());
    CWLJobApp app = BeanSerializer.deserialize(resolvedAppStr, CWLJobApp.class);
    CWLJob cwlJob =  new CWLJobProcessor().process(new CWLJob(job.getName(), app, job.getInputs(), job.getOutputs()));
    
    if (job.getResources() != null) {
      CWLRuntime runtime = null;
      try {
        runtime = CWLRuntimeHelper.createRuntime(cwlJob);
        cwlJob.setRuntime(runtime);
      } catch (CWLExpressionException e1) {
        throw new BindingException(e1);
      }
    }
    return cwlJob;
  }
  
}
