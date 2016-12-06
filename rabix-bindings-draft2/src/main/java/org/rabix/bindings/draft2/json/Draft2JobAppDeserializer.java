package org.rabix.bindings.draft2.json;

import java.io.IOException;

import org.rabix.bindings.draft2.bean.Draft2CommandLineTool;
import org.rabix.bindings.draft2.bean.Draft2EmbeddedApp;
import org.rabix.bindings.draft2.bean.Draft2ExpressionTool;
import org.rabix.bindings.draft2.bean.Draft2JobApp;
import org.rabix.bindings.draft2.bean.Draft2PythonTool;
import org.rabix.bindings.draft2.bean.Draft2Workflow;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Draft2JobAppDeserializer extends JsonDeserializer<Draft2JobApp> {

  private static final String CLASS_KEY = "class";
  private static final String WORKFLOW_CLASS = "Workflow";
  private static final String COMMANDLINETOOL_CLASS = "CommandLineTool";
  private static final String PYTHONTOOL_CLASS = "PythonTool";
  private static final String EXPRESSION_CLASS = "ExpressionTool";
  
  @Override
  public Draft2JobApp deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
    ;
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isObject()) {
      if (tree.get(CLASS_KEY).asText().equals(WORKFLOW_CLASS)) {
        return objectMapper.readValue(JSONHelper.writeObject(tree), Draft2Workflow.class);
      } else if (tree.get(CLASS_KEY).asText().equals(COMMANDLINETOOL_CLASS)) {
        return objectMapper.readValue(JSONHelper.writeObject(tree), Draft2CommandLineTool.class);
      } else if (tree.get(CLASS_KEY).asText().equals(PYTHONTOOL_CLASS)) {
        return objectMapper.readValue(JSONHelper.writeObject(tree), Draft2PythonTool.class);
      } else if (tree.get(CLASS_KEY).asText().equals(EXPRESSION_CLASS)) {
        return objectMapper.readValue(JSONHelper.writeObject(tree), Draft2ExpressionTool.class);
      }
    }
    return new Draft2EmbeddedApp(JSONHelper.writeObject(tree));
  }

}
