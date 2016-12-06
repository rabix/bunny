package org.rabix.bindings.sb.json;

import java.io.IOException;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.sb.bean.SBCommandLineTool;
import org.rabix.bindings.sb.bean.SBEmbeddedApp;
import org.rabix.bindings.sb.bean.SBExpressionTool;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBPythonTool;
import org.rabix.bindings.sb.bean.SBWorkflow;
import org.rabix.bindings.sb.resolver.SBDocumentResolver;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SBJobAppDeserializer extends JsonDeserializer<SBJobApp>  {
  
  private static final String CLASS_KEY = "class";
  private static final String WORKFLOW_CLASS = "Workflow";
  private static final String COMMANDLINETOOL_CLASS = "CommandLineTool";
  private static final String PYTHONTOOL_CLASS = "PythonTool";
  private static final String EXPRESSION_CLASS = "ExpressionTool";

  @Override
  public SBJobApp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) p.getCodec();;
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isObject()) {
      if(tree.get(SBDocumentResolver.CWL_VERSION_KEY) == null || tree.get(SBDocumentResolver.CWL_VERSION_KEY).asText().equals(ProtocolType.SB.appVersion)) {
        if(tree.get(CLASS_KEY).asText().equals(WORKFLOW_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), SBWorkflow.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(COMMANDLINETOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), SBCommandLineTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(PYTHONTOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), SBPythonTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(EXPRESSION_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), SBExpressionTool.class);
        }
      }
    }
    return new SBEmbeddedApp(JSONHelper.writeObject(tree));
  }
  
}
