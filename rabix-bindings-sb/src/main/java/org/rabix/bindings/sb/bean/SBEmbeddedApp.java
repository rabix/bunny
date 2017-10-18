package org.rabix.bindings.sb.bean;

import java.io.IOException;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.bean.SBEmbeddedApp.SBEmbeddedAppSerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@JsonDeserialize(as = SBEmbeddedApp.class)
@JsonSerialize(using = SBEmbeddedAppSerializer.class)
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
      throw new RuntimeException();
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
  @JsonIgnore
  public SBJobAppType getType() {
    return SBJobAppType.EMBEDDED;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((application == null) ? 0 : application.hashCode());
    result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
    result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SBEmbeddedApp other = (SBEmbeddedApp) obj;
    if (application == null) {
      if (other.application != null)
        return false;
    } else if (!application.equals(other.application))
      return false;
    if (inputs == null) {
      if (other.inputs != null)
        return false;
    } else if (!inputs.equals(other.inputs))
      return false;
    if (outputs == null) {
      if (other.outputs != null)
        return false;
    } else if (!outputs.equals(other.outputs))
      return false;
    return true;
  }

  public static class SBEmbeddedAppSerializer extends JsonSerializer<SBEmbeddedApp> {
    @Override
    public void serialize(SBEmbeddedApp value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      gen.writeObject(value.application);
    }
  }
}
