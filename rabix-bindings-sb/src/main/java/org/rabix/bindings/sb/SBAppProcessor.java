package org.rabix.bindings.sb;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBJobAppType;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.resolver.SBDocumentResolver;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;

public class SBAppProcessor implements ProtocolAppProcessor {

  @Override
  public JsonNode loadApp(String uri) throws BindingException {
    return SBDocumentResolver.resolve(uri);
  }
  
  @Override
  public Application loadAppObject(String app) throws BindingException {
    return JSONHelper.readObject(loadApp(app), SBJobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    SBJobApp app = (SBJobApp) loadAppObject(job.getApp());
    return app.getType().equals(SBJobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    SBJobApp sbJobApp = sbJob.getApp();
    for (SBInputPort inputPort : sbJobApp.getInputs()) {
      if (SBSchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = SBSchemaHelper.normalizeId(inputPort.getId());
        if (!sbJob.getInputs().containsKey(inputPortId)) {
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
