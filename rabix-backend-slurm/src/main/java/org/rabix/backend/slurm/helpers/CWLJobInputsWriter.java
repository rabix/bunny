package org.rabix.backend.slurm.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CWLJobInputsWriter {
    private final static Logger logger = LoggerFactory.getLogger(CWLJobInputsWriter.class);

    /**
     * creates CWL inputs.json inside @baseDir from @job's inputs
     */
    public static File createInputsFile(Job job, File baseDir){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        Map<String, Object> inputs = job.getInputs();
        rootNode = processHashMapNode(rootNode, inputs, mapper);
        File inputsFile = new File(baseDir, "inputs.json");
        try {
            mapper.writeValue(inputsFile, rootNode);
        }catch (IOException e){
            e.printStackTrace();
        }
        return inputsFile;
    }


    private static JsonNode processFileValue(Object childNode, FileValue value){
        ObjectNode childNode2 = (ObjectNode) childNode;
        childNode2.put("class", "File");
        if (value.getPath() != null){
        childNode2.put("path", value.getPath());}

        if (value.getLocation() != null){
            ((ObjectNode) childNode2).put("location", value.getLocation());
        }
        return childNode2;
    }

    @SuppressWarnings("unchecked")
    private static ArrayNode processArray(ArrayNode childArrayNode, ObjectMapper mapper, Object objectValue){
        ArrayList array = (ArrayList) objectValue;
        for (Object obj: array) {
            if (obj instanceof FileValue) {
                JsonNode childNode3 = mapper.createObjectNode();
                FileValue value = (FileValue) obj;
                childArrayNode.add(processFileValue(childNode3, value));
            }else if (obj instanceof Integer){
                Integer value = (Integer) obj;
                childArrayNode.add(value);
            }else if (obj instanceof Long){
                Long value = (Long) obj;
                childArrayNode.add(value);
            }else if (obj instanceof String){
                String value = (String) obj;
                childArrayNode.add(value);
            }else if (obj == null){
                String value = (String) obj;
                childArrayNode.add(value);
            }else if (obj instanceof Map){
                ObjectMapper m = new ObjectMapper();
                Map<String,Object> mappedObject = m.convertValue(obj, Map.class);
                JsonNode childNode = mapper.createObjectNode();
                childNode = processHashMapNode(childNode, mappedObject, mapper);
                childArrayNode.add(childNode);
            }else if (obj instanceof ArrayList){
                ArrayNode stepsonArrayNode = mapper.createArrayNode();
                stepsonArrayNode = processArray(stepsonArrayNode, mapper, obj);
                childArrayNode.add(stepsonArrayNode);
            }
            else{
                System.exit(12);
                throw new NotImplementedException();

            }
        }
        return childArrayNode;
    }

    private static JsonNode processHashMapNode(JsonNode rootNode, Map<String, Object> inputs, ObjectMapper mapper){
        for (Map.Entry<String, Object> input: inputs.entrySet()){
            JsonNode childNode2 = mapper.createObjectNode();
            Object objectValue = input.getValue();
            if (objectValue instanceof FileValue) {
                FileValue value = (FileValue) objectValue;
                processFileValue(childNode2, value);
                ((ObjectNode) rootNode).set(input.getKey(), childNode2);
            } else if (objectValue instanceof String){
                String value = (String) objectValue;
                ((ObjectNode) rootNode).put(input.getKey(), value);
            } else if (objectValue instanceof Boolean){
                Boolean value = (Boolean) objectValue;
                ((ObjectNode) rootNode).put(input.getKey(), value);
            }else if (objectValue instanceof Integer){
                Integer value = (Integer) objectValue;
                ((ObjectNode) rootNode).put(input.getKey(), value);
            }else if (objectValue instanceof Long){
                Long value = (Long) objectValue;
                ((ObjectNode) rootNode).put(input.getKey(), value);
            }else if (objectValue == null){
            }else if (objectValue instanceof ArrayList){
                ArrayNode childArrayNode = mapper.createArrayNode();
                childArrayNode = processArray(childArrayNode, mapper, objectValue);
                ((ObjectNode) rootNode).put(input.getKey(), childArrayNode);
            }else if (objectValue instanceof Map) {
                ObjectMapper m = new ObjectMapper();
                Map<String, Object> mappedObject = m.convertValue(objectValue, Map.class);
                JsonNode childNode = mapper.createObjectNode();
                childNode = processHashMapNode(childNode, mappedObject, mapper);
                ((ObjectNode) rootNode).set(input.getKey(), childNode);
            }else{
                logger.error("Not implemented input type: " + input.toString());
                System.exit(12);
                throw new NotImplementedException();
            }
        }
        return rootNode;
    }
}
