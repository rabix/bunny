package org.rabix.bindings.draft2.helper;

import java.nio.file.Paths;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft2.Draft2JobProcessor;
import org.rabix.bindings.draft2.Draft2ValueTranslator;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2JobApp;
import org.rabix.bindings.draft2.bean.Draft2Resources;
import org.rabix.bindings.draft2.resolver.Draft2DocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class Draft2JobHelper {

  @SuppressWarnings("unchecked")
  public static Draft2Job getDraft2Job(Job job) throws BindingException {
    String appPath = job.getApp();
    String resolvedAppStr = Draft2DocumentResolver.resolve(appPath);
    Draft2JobApp app = BeanSerializer.deserialize(resolvedAppStr, Draft2JobApp.class);
    if (appPath.startsWith("file")) {
      app.setAppFileLocation(Paths.get(appPath.replaceAll("file:", "")).toString());
    }
    Map<String, Object> nativeInputs = (Map<String, Object>) Draft2ValueTranslator.translateToSpecific(job.getInputs());
    Map<String, Object> nativeOutputs = (Map<String, Object>) Draft2ValueTranslator.translateToSpecific(job.getOutputs());
    
    Draft2Job sbJob = new Draft2JobProcessor().process(new Draft2Job(job.getName(), app, nativeInputs, nativeOutputs));

    if (job.getResources() != null) {
      Draft2Resources sbResources = new Draft2Resources(false, job.getResources().getCpu(), job.getResources().getMemMB());
      sbJob.setResources(sbResources);
    }
    return sbJob;
  }

}
