package org.rabix.bindings.cwl.json.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CWLResourcesDeserializer extends JsonDeserializer<List<CWLResource>> {

  @Override
  public List<CWLResource> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    List<CWLResource> resources = new ArrayList<>();
    if (tree.isArray()) {
      for (JsonNode node : tree) {
        resources.add(BeanSerializer.deserialize(node.toString(), CWLResource.class));
      }
      return resources;
    }
    if (tree.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();
      
      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        CWLResource resource = BeanSerializer.deserialize(subnodeEntry.getValue().toString(), CWLResource.class);
        resource.add(CWLSchemaHelper.KEY_JOB_TYPE, subnodeEntry.getKey());
        resources.add(resource);
      }
      return resources;
    }
    return null;
  }

}
