package org.rabix.bindings.draft2.bean;

import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Draft2EmbeddedApp extends Draft2JobApp {

  private Application application;
  private List<Draft2InputPort> inputs;
  private List<Draft2OutputPort> outputs;

  @JsonCreator
  public Draft2EmbeddedApp(String raw) {
    try {
      Bindings bindings = BindingsFactory.create(raw);

      application = bindings.loadAppObject(raw);
      inputs = Lists.transform(application.getInputs(), new Function<ApplicationPort, Draft2InputPort>() {
        @Override
        public Draft2InputPort apply(ApplicationPort port) {
          return new Draft2InputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge(), port.getDescription());
        }
      });
      outputs = Lists.transform(application.getOutputs(), new Function<ApplicationPort, Draft2OutputPort>() {
        @Override
        public Draft2OutputPort apply(ApplicationPort port) {
          return new Draft2OutputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge(), port.getDescription());
        }
      });
    } catch (BindingException e1) {
      // TOOD implement
    }
  }
  
  @Override
  public List<Draft2InputPort> getInputs() {
    return inputs;
  }
  
  @Override
  public List<Draft2OutputPort> getOutputs() {
    return outputs;
  }
  
  @Override
  public String serialize() {
    return application.serialize();
  }
  
  @Override
  public Draft2JobAppType getType() {
    return Draft2JobAppType.EMBEDDED;
  }

}
