package org.rabix.bindings.cwl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CWLStepPortsDeserializer extends JsonDeserializer<List<Map<String, Object>>> {
  @Override
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    
    List<Map<String, Object>> stepPorts = new ArrayList<>();
    
    if (tree.isArray()) {
      List<Object> list = (List<Object>) JSONHelper.transform(tree, false);
      for (Object listObj : list) {
        if (listObj instanceof String) {
          Map<String, Object> single = new HashMap<>();
          single.put(CWLSchemaHelper.STEP_PORT_ID, listObj);
          stepPorts.add(single);
          continue;
        }
        stepPorts.add((Map<String, Object>) listObj);
      }
      return stepPorts;
    }

    if (tree.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();

      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        Map<String, Object> stepPort = new HashMap<>();
        
        if (subnodeEntry.getValue().isObject()) {
          stepPort = JSONHelper.readMap(subnodeEntry.getValue());
        } else {
          stepPort = new HashMap<>();
          stepPort.put(CWLBindingHelper.KEY_SOURCE, JSONHelper.transform(subnodeEntry.getValue(), false));
        }
        stepPort.put(CWLSchemaHelper.STEP_PORT_ID, subnodeEntry.getKey());        
        stepPorts.add(stepPort);
      }
      return stepPorts;
    }
    return null;
  }
}
