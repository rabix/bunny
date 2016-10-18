package org.rabix.bindings.cwl.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.CWLJobProcessor;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class CWLJobHelper {

  public static CWLJob getCWLJob(Job job) throws BindingException {
    String resolvedAppStr = CWLDocumentResolver.resolve(job.getApp());
    CWLJobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), CWLJobApp.class);
    CWLJob cwlJob =  new CWLJobProcessor().process(new CWLJob(InternalSchemaHelper.normalizeId(job.getName()), app, job.getInputs(), job.getOutputs()));
    
    if (job.getResources() != null) {
      CWLRuntime cwlRuntime = new CWLRuntime(job.getResources().getCpu(), job.getResources().getMemMB(), job.getResources().getWorkingDir(), job.getResources().getWorkingDir(), job.getResources().getDiskSpaceMB(), job.getResources().getDiskSpaceMB());
      cwlJob.setRuntime(cwlRuntime);
    }
    return cwlJob;
  }
  
}
