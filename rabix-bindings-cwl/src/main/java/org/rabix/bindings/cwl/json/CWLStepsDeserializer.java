package org.rabix.bindings.cwl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLStep;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class CWLStepsDeserializer extends JsonDeserializer<List<CWLStep>> {
  
  @Override
  public List<CWLStep> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isArray()) {
      return JSONHelper.readObject(tree.toString(), new TypeReference<List<CWLStep>>() {});
    }

    if (tree.isObject()) {
      List<CWLStep> steps = new ArrayList<>();
      Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();

      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        ((ObjectNode)subnodeEntry.getValue()).set(CWLSchemaHelper.STEP_PORT_ID, new TextNode(subnodeEntry.getKey()));
        CWLStep step = JSONHelper.readObject(subnodeEntry.getValue(), CWLStep.class);
        steps.add(step);
      }
      return steps;
    }
    return null;
  }
}
