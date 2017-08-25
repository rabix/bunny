package org.rabix.bindings.draft2.bean;

import java.io.IOException;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.draft2.bean.Draft2EmbeddedApp.Draft2EmbededAppSerializer;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@JsonDeserialize(as = Draft2EmbeddedApp.class)
@JsonSerialize(as = Draft2EmbededAppSerializer.class)
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
      throw new RuntimeException();
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
  public Draft2JobAppType getType() {
    return Draft2JobAppType.EMBEDDED;
  }

  public static class Draft2EmbededAppSerializer extends JsonSerializer<Draft2EmbeddedApp> {
    @Override
    public void serialize(Draft2EmbeddedApp value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      gen.writeObject(value.application);
    }
  }
}
