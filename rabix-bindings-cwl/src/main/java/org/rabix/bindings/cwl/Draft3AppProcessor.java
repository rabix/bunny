package org.rabix.bindings.draft3;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3JobAppType;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.resolver.Draft3DocumentResolver;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class Draft3AppProcessor implements ProtocolAppProcessor {

  @Override
  public String loadApp(String uri) throws BindingException {
    return Draft3DocumentResolver.resolve(uri);
  }
  
  @Override
  public Application loadAppObject(String app) throws BindingException {
    return BeanSerializer.deserialize(loadApp(app), Draft3JobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    Draft3JobApp app = (Draft3JobApp) loadAppObject(job.getApp());
    return app.getType().equals(Draft3JobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    Draft3JobApp draft2JobApp = draft2Job.getApp();
    for (Draft3InputPort inputPort : draft2JobApp.getInputs()) {
      if (Draft3SchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = Draft3SchemaHelper.normalizeId(inputPort.getId());
        if (!draft2Job.getInputs().containsKey(inputPortId)) {
          builder.append(throwException ? "," : "").append(inputPortId);
          throwException = true;
        }
      }
    }
    if (throwException) {
      throw new BindingException(builder.toString());
    }
  }
}
