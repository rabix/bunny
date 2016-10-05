package org.rabix.bindings.cwl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CWLOutputPortsDeserializer extends JsonDeserializer<List<CWLOutputPort>> {
  @Override
  public List<CWLOutputPort> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    List<CWLOutputPort> inputPorts = new ArrayList<>();
    if (tree.isArray()) {
      for (JsonNode node : tree) {
        inputPorts.add(BeanSerializer.deserialize(node.toString(), CWLOutputPort.class));
      }
      return inputPorts;
    }
    if (tree.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();
      
      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        CWLOutputPort outputPort = null;
        if (subnodeEntry.getValue().isObject()) {
          outputPort = BeanSerializer.deserialize(subnodeEntry.getValue().toString(), CWLOutputPort.class);
          outputPort.setId(subnodeEntry.getKey());
        } else {
          outputPort = new CWLOutputPort(subnodeEntry.getKey(), null, null, subnodeEntry.getValue(), null, null, null, null, null);
        }
        inputPorts.add(outputPort);
      }
      return inputPorts;
    }
    return null;
  }
}