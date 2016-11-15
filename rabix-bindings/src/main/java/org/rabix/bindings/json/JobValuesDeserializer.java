package org.rabix.bindings.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class JobValuesDeserializer extends JsonDeserializer<Map<String, Object>> {

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    Map<String, Object> values = p.getCodec().readValue(p, Map.class);
    if (values == null) {
      return null;
    }
    return (Map<String, Object>) transform(values);
  }
  
  @SuppressWarnings("unchecked")
  private Object transform(Object value) {
    if (value instanceof Map<?, ?>) {
      if (DirectoryValue.isDirectoryValue(value)) {
        return DirectoryValue.fromMap(value);
      }
      if (FileValue.isFileValue(value)) {
        return FileValue.fromMap(value);
      }
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        entry.setValue(transform(entry.getValue()));
      }
      return value;
    }
    if (value instanceof List<?>) {
      List<Object> newList = new ArrayList<>();
      for (Object item : (List<?>) value) {
        newList.add(transform(item));
      }
      return newList;
    }
    return value;
  }

}
