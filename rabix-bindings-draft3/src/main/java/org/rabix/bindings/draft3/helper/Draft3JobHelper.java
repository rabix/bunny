package org.rabix.bindings.draft3.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft3.Draft3JobProcessor;
import org.rabix.bindings.draft3.Draft3ValueTranslator;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3Runtime;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.resolver.Draft3DocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class Draft3JobHelper {

  @SuppressWarnings("unchecked")
  public static Draft3Job getDraft3Job(Job job) throws BindingException {
    String resolvedAppStr = Draft3DocumentResolver.resolve(job.getApp());
    Draft3JobApp app = BeanSerializer.deserialize(resolvedAppStr, Draft3JobApp.class);
    
    Map<String, Object> nativeInputs = (Map<String, Object>) Draft3ValueTranslator.translateToSpecific(job.getInputs());
    Map<String, Object> nativeOutputs = (Map<String, Object>) Draft3ValueTranslator.translateToSpecific(job.getOutputs());
    
    Draft3Job draft3Job =  new Draft3JobProcessor().process(new Draft3Job(job.getName(), app, nativeInputs, nativeOutputs));
    
    Draft3Runtime runtime;
    try {
      runtime = Draft3RuntimeHelper.createRuntime(draft3Job, job.getResources());
      draft3Job.setRuntime(runtime);
    } catch (Draft3ExpressionException e) {
      throw new BindingException(e);
    }
    return draft3Job;
  }
}
