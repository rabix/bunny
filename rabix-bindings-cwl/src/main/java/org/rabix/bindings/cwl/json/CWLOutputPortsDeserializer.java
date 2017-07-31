package org.rabix.bindings.cwl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.common.helper.JSONHelper;
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
    List<CWLOutputPort> outputPorts = new ArrayList<>();
    if (tree.isArray()) {
      for (JsonNode node : tree) {
        outputPorts.add(BeanSerializer.deserialize(node.toString(), CWLOutputPort.class));
      }
      return outputPorts;
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
          outputPort = new CWLOutputPort(subnodeEntry.getKey(), null, null, JSONHelper.transform(subnodeEntry.getValue(), false), null, null, null, null, null, null);
        }
        outputPort = handleRedirection(outputPort, CWLCommandLineTool.STDOUT_KEY);
        outputPort = handleRedirection(outputPort, CWLCommandLineTool.STDERR_KEY);
        outputPorts.add(outputPort);
      }
      return outputPorts;
    }
    return null;
  }
  
  private CWLOutputPort handleRedirection(CWLOutputPort outputPort, String redirection) {
    Object type = outputPort.getSchema();
    if (type instanceof String && type.equals(redirection)) {
      Map<String, Object> outputBinding = new HashMap<>();
      if (CWLCommandLineTool.STDOUT_KEY.equals(redirection)) {
        outputBinding.put(CWLBindingHelper.KEY_GLOB, CWLCommandLineTool.generateRandomStdoutGlob());  
      } else if (CWLCommandLineTool.STDERR_KEY.equals(redirection)) {
        outputBinding.put(CWLBindingHelper.KEY_GLOB, CWLCommandLineTool.generateRandomStderrGlob());
      }
      return new CWLOutputPort(outputPort.getId(), null, null, CWLSchemaHelper.TYPE_JOB_FILE, outputBinding, null, null, null, null, null);
    }
    return outputPort;
  }
}