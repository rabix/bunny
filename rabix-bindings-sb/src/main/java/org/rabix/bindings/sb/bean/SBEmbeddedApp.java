package org.rabix.bindings.sb.bean;

import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@JsonDeserialize(as = SBEmbeddedApp.class)
public class SBEmbeddedApp extends SBJobApp {

  private Application application;
  private List<SBInputPort> inputs;
  private List<SBOutputPort> outputs;

  @JsonCreator
  public SBEmbeddedApp(String raw) {
    try {
      Bindings bindings = BindingsFactory.create(raw);

      application = bindings.loadAppObject(raw);
      inputs = Lists.transform(application.getInputs(), new Function<ApplicationPort, SBInputPort>() {
        @Override
        public SBInputPort apply(ApplicationPort port) {
          return new SBInputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge(), port.getDescription());
        }
      });
      outputs = Lists.transform(application.getOutputs(), new Function<ApplicationPort, SBOutputPort>() {
        @Override
        public SBOutputPort apply(ApplicationPort port) {
          return new SBOutputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge(), port.getDescription());
        }
      });
    } catch (BindingException e1) {
      // TOOD implement
    }
  }
  
  @Override
  public List<SBInputPort> getInputs() {
    return inputs;
  }
  
  @Override
  public List<SBOutputPort> getOutputs() {
    return outputs;
  }
  
  @Override
  public String serialize() {
    return application.serialize();
  }
  
  @Override
  public SBJobAppType getType() {
    return SBJobAppType.EMBEDDED;
  }

}
