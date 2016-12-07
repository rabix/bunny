package org.rabix.bindings.cwl.json;

import java.io.IOException;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLEmbeddedApp;
import org.rabix.bindings.cwl.bean.CWLExpressionTool;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLPythonTool;
import org.rabix.bindings.cwl.bean.CWLWorkflow;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CWLJobAppDeserializer extends JsonDeserializer<CWLJobApp> {

  private static final String CLASS_KEY = "class";
  private static final String WORKFLOW_CLASS = "Workflow";
  private static final String COMMANDLINETOOL_CLASS = "CommandLineTool";
  private static final String PYTHONTOOL_CLASS = "PythonTool";
  private static final String EXPRESSION_CLASS = "ExpressionTool";  
  
  @Override
  public CWLJobApp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) p.getCodec();;
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isObject()) {
      if(tree.get(CWLDocumentResolver.CWL_VERSION_KEY) == null || tree.get(CWLDocumentResolver.CWL_VERSION_KEY).asText().equals(ProtocolType.CWL.appVersion)) {
        if(tree.get(CLASS_KEY).asText().equals(WORKFLOW_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), CWLWorkflow.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(COMMANDLINETOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), CWLCommandLineTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(PYTHONTOOL_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), CWLPythonTool.class);
        }
        else if(tree.get(CLASS_KEY).asText().equals(EXPRESSION_CLASS)) {
          return objectMapper.readValue(JSONHelper.writeObject(tree), CWLExpressionTool.class);
        }
      }
    }
    return new CWLEmbeddedApp(JSONHelper.writeObject(tree));
  }
  
  

}
