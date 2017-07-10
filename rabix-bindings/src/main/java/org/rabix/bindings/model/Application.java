package org.rabix.bindings.model;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using=Application.ApplicationDeserializer.class)
public abstract class Application {

  @JsonIgnore
  public abstract String serialize();

  @JsonIgnore
  public abstract ApplicationPort getInput(String name);
  
  @JsonIgnore
  public abstract ApplicationPort getOutput(String name);
  
  @JsonIgnore
  public abstract List<? extends ApplicationPort> getInputs();
  
  @JsonIgnore
  public abstract List<? extends ApplicationPort> getOutputs();

  @JsonIgnore
  public abstract String getVersion();

  @JsonIgnore
  public abstract ValidationReport validate();
  
  protected Map<String, Object> raw = new HashMap<>();
  
  @JsonAnySetter
  public void add(String key, Object value) {
    raw.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getRaw() {
    return raw;
  }

  @JsonIgnore
  public Object getProperty(String key) {
    return raw.get(key);
  }

  /**
   * Checks if provided inputs are valid for this app.
   * Also checks if all required inputs are present
   * @param inputValues potential input values
   * @return null if everything is OK. If there are errors, it returns map:
   * Key - ID of ApplicationPort (or input value) which is not valid
   * Value - Reason for validation falure
   */
  public Map<String, String> validateInputs(Map<String, Object> inputValues) {
    Map<String, String> ret = new HashMap<>();
    for (ApplicationPort input : getInputs()) {
      String inputId = input.getId().replaceFirst("^#", "");
      Object inputValue = inputValues.get(inputId);
      if (inputValue==null && input.isRequired() && input.getDefaultValue()==null) {
        ret.put(inputId, "Required field");
      } else {
        String s = input.validate(inputValue);
        if (s!=null)
          ret.put(inputId, s);
      }
    }

    for (String key : inputValues.keySet()) {
      if (getInput(key) == null)
        ret.put(key, "Invalid input");
    }

    if (!ret.isEmpty())
      return ret;
    return null;
  }

  public Set<String> checkPortDuplicates(List<? extends ApplicationPort> ports) {
    Set<String> duplicates = new HashSet<>();
    Set<String> ids = new HashSet<>();
    for (ApplicationPort port: ports) {
      if (!ids.add(port.getId())) {
        duplicates.add(port.getId());
      }
    }
    return duplicates;
  }

  protected List<String> validatePortUniqueness() {
    List<String> validationErrors = new ArrayList<>();

    for (String duplicate : checkPortDuplicates(getInputs())) {
      validationErrors.add("Duplicate input id: " + duplicate);
    }

    for (String duplicate : checkPortDuplicates(getOutputs())) {
      validationErrors.add("Duplicate output id: " + duplicate);
    }

    return validationErrors;
  }

  public static class ApplicationDeserializer extends JsonDeserializer<Application> {
    @Override
    public Application deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNode tree = p.getCodec().readTree(p);
      if (tree.isNull()) {
        return null;
      }
      String appUrl = URIHelper.createDataURI(JSONHelper.writeObject(tree));
      try {
        return BindingsFactory.create(appUrl).loadAppObject(appUrl);
      } catch (BindingException e) {
        throw new IOException("Failed to deserialize Application " + tree);
      }
    }
  }
  
  public static class ApplicationSerializer extends JsonSerializer<Application> {
    @Override
    public void serialize(Application value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      if (value == null) {
        gen.writeNull();
      } else {
        JsonNode node = JSONHelper.readJsonNode(value.serialize());
        gen.writeTree(node);
      }
    }
  }
}
