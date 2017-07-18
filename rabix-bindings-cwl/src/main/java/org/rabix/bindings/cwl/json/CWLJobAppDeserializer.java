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
    ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
    JsonNode tree = p.getCodec().readTree(p);
    if (tree.isNull()) {
      return null;
    }
    if (tree.isObject()) {
      if(tree.get(CWLDocumentResolver.CWL_VERSION_KEY) == null || tree.get(CWLDocumentResolver.CWL_VERSION_KEY).asText().equals(ProtocolType.CWL.appVersion)) {
        JsonNode classNode = tree.get(CLASS_KEY);
        
        if (classNode == null)
          throw new IllegalStateException("\"" + CLASS_KEY + "\" attribute missing!");

        if(classNode.asText().equals(WORKFLOW_CLASS)) {
          return objectMapper.treeToValue(tree, CWLWorkflow.class);
        }
        else if(classNode.asText().equals(COMMANDLINETOOL_CLASS)) {
          return objectMapper.treeToValue(tree, CWLCommandLineTool.class);
        }
        else if(classNode.asText().equals(PYTHONTOOL_CLASS)) {
          return objectMapper.treeToValue(tree, CWLPythonTool.class);
        }
        else if(classNode.asText().equals(EXPRESSION_CLASS)) {
          return objectMapper.treeToValue(tree, CWLExpressionTool.class);
        } else {
          throw new IllegalStateException("Ivalid value for \"" + CLASS_KEY + "\" attribute!");
        }
      }
    }
    return new CWLEmbeddedApp(JSONHelper.writeObject(tree));
  }
  
  

}
