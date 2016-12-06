package org.rabix.bindings.draft3.json;

import java.io.IOException;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.draft3.bean.Draft3EmbeddedApp;
import org.rabix.bindings.draft3.bean.Draft3ExpressionTool;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3PythonTool;
import org.rabix.bindings.draft3.bean.Draft3Workflow;
import org.rabix.bindings.draft3.resolver.Draft3DocumentResolver;
import org.rabix.common.helper.JSONHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Draft3JobAppDeserializer extends JsonDeserializer<Draft3JobApp> {

  private static final String CLASS_KEY = "class";
  private static final String WORKFLOW_CLASS = "Workflow";
  private static final String COMMANDLINETOOL_CLASS = "CommandLineTool";
  private static final String PYTHONTOOL_CLASS = "PythonTool";
  private static final String EXPRESSION_CLASS = "ExpressionTool";
  
  @Override
  public Draft3JobApp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) p.getCodec();;
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isObject()) {
      if(tree.get(Draft3DocumentResolver.CWL_VERSION_KEY) == null || tree.get(Draft3DocumentResolver.CWL_VERSION_KEY).asText().equals(ProtocolType.DRAFT3.appVersion)) {
        if(tree.get(CLASS_KEY).asText().equals(WORKFLOW_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), Draft3Workflow.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(COMMANDLINETOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), Draft3CommandLineTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(PYTHONTOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), Draft3PythonTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(EXPRESSION_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), Draft3ExpressionTool.class);
        }
      }
    }
    return new Draft3EmbeddedApp(JSONHelper.writeObject(tree));
  }

}
