package org.rabix.bindings.sb.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.SBJobProcessor;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBResources;
import org.rabix.bindings.sb.resolver.SBDocumentResolver;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class SBJobHelper {

  public static SBJob getSBJob(Job job) throws BindingException {
    String resolvedAppStr = SBDocumentResolver.resolve(job.getApp());
    SBJobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), SBJobApp.class);
    SBJob sbJob = new SBJobProcessor().process(new SBJob(InternalSchemaHelper.getNativeIdFromName(job.getName()), app, job.getInputs(), job.getOutputs()));

    if (job.getResources() != null) {
      SBResources sbResources = new SBResources(false, job.getResources().getCpu(), job.getResources().getMemMB());
      sbJob.setResources(sbResources);
    }
    return sbJob;
  }

}
