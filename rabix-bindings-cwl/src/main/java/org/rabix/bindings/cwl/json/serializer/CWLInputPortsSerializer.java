package org.rabix.bindings.cwl.json.serializer;

import java.io.IOException;
import java.util.List;

import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CWLInputPortsSerializer extends JsonSerializer<List<CWLInputPort>> {
  @Override
  public void serialize(List<CWLInputPort> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
    if (value == null) {
      gen.writeNull();
    }
    JsonNode node = JSONHelper.readJsonNode(BeanSerializer.serializeFull(value));
    gen.writeTree(node);
  }
}