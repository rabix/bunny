package org.rabix.bindings.cwl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CWLInputPortsDeserializer extends JsonDeserializer<List<CWLInputPort>> {
  @Override
  public List<CWLInputPort> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    List<CWLInputPort> inputPorts = new ArrayList<>();
    if (tree.isArray()) {
      for (JsonNode node : tree) {
        inputPorts.add(BeanSerializer.deserialize(node.toString(), CWLInputPort.class));  
      }
      return inputPorts;
    }
    if (tree.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();
      
      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        CWLInputPort inputPort = null;
        if (subnodeEntry.getValue().isObject()) {
          if(subnodeEntry.getValue().has("type") && subnodeEntry.getValue().get("type").equals("record")) {
            inputPort = new CWLInputPort(subnodeEntry.getKey(), null, JSONHelper.transform(subnodeEntry.getValue(), false), null, null, null, null, null, null, null, null);
          } else {
            inputPort = BeanSerializer.deserialize(subnodeEntry.getValue().toString(), CWLInputPort.class);
            inputPort.setId(subnodeEntry.getKey());
          }
        } else {
          inputPort = new CWLInputPort(subnodeEntry.getKey(), null, JSONHelper.transform(subnodeEntry.getValue(), false), null, null, null, null, null, null, null, null);
        }
        inputPorts.add(inputPort);
      }
      return inputPorts;
    }
    return null;
  }
}

