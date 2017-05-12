package org.rabix.bindings.cwl;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLJobAppType;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ValidationReport;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class CWLAppProcessor implements ProtocolAppProcessor {

  @Override
  public String loadApp(String uri) throws BindingException {
    return CWLDocumentResolver.resolve(uri);
  }
  
  @Override
  public Application loadAppObject(String app) throws BindingException {
    return BeanSerializer.deserialize(loadApp(app), CWLJobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    CWLJobApp app = (CWLJobApp) loadAppObject(job.getApp());
    return app.getType().equals(CWLJobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    CWLJobApp cwlJobApp = cwlJob.getApp();

    ValidationReport validation = cwlJobApp.validate();

    if (!validation.getErrors().isEmpty()) {
      throw new BindingException(String.join("\n", validation.getErrors()));
    }

    for (CWLInputPort inputPort : cwlJobApp.getInputs()) {
      if (CWLSchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = CWLSchemaHelper.normalizeId(inputPort.getId());
        if (!cwlJob.getInputs().containsKey(inputPortId)) {
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
