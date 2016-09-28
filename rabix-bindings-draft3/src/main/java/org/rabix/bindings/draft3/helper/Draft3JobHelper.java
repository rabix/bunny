package org.rabix.bindings.draft3.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft3.Draft3JobProcessor;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3Runtime;
import org.rabix.bindings.draft3.resolver.Draft3DocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft3JobHelper {

  public static Draft3Job getDraft3Job(Job job) throws BindingException {
    String resolvedAppStr = Draft3DocumentResolver.resolve(job.getApp());
    Draft3JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft3JobApp.class);
    Draft3Job draft3Job =  new Draft3JobProcessor().process(new Draft3Job(app, job.getInputs()));
    
    if (job.getResources() != null) {
      Draft3Runtime draft3Runtime = new Draft3Runtime(job.getResources().getCpu(), job.getResources().getMemMB(), job.getResources().getWorkingDir(), job.getResources().getWorkingDir(), job.getResources().getDiskSpaceMB(), job.getResources().getDiskSpaceMB());
      draft3Job.setRuntime(draft3Runtime);
    }
    return draft3Job;
  }
}
