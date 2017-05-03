package org.rabix.bindings.sb.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.SBJobProcessor;
import org.rabix.bindings.sb.SBValueTranslator;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBResources;
import org.rabix.bindings.sb.resolver.SBDocumentResolver;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;

public class SBJobHelper {

  @SuppressWarnings("unchecked")
  public static SBJob getSBJob(Job job) throws BindingException {
    JsonNode resolvedApp = SBDocumentResolver.resolve(job.getApp());
    SBJobApp app = JSONHelper.readObject(resolvedApp, SBJobApp.class);
    
    Map<String, Object> nativeInputs = (Map<String, Object>) SBValueTranslator.translateToSpecific(job.getInputs());
    Map<String, Object> nativeOutputs = (Map<String, Object>) SBValueTranslator.translateToSpecific(job.getOutputs());
    
    SBJob sbJob = new SBJobProcessor().process(new SBJob(job.getName(), app, nativeInputs, nativeOutputs));

    if (job.getResources() != null) {
      SBResources sbResources = new SBResources(false, job.getResources().getCpu(), job.getResources().getMemMB());
      sbJob.setResources(sbResources);
    }
    return sbJob;
  }

}
