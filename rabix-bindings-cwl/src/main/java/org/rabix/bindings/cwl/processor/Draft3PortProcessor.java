package org.rabix.bindings.draft3.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft3PortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(Draft3PortProcessor.class);
  
  private Draft3Job job;
  
  public Draft3PortProcessor(Draft3Job job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, Draft3PortProcessorCallback portProcessor) throws Draft3PortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), Draft3InputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, Draft3PortProcessorCallback portProcessor) throws Draft3PortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), Draft3OutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, Draft3PortProcessorCallback portProcessor) throws Draft3PortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(Draft3SchemaHelper.normalizeId(id), clazz);
      if (port == null) {
        throw new Draft3PortProcessorException("Port with ID=" + Draft3SchemaHelper.normalizeId(id) + " doesn't exist.");
      }
      Object mappedValue = null;
      try {
        mappedValue = processValue(value, port, port.getSchema(), Draft3SchemaHelper.normalizeId(id), portProcessor);
      } catch (Exception e) {
        throw new Draft3PortProcessorException("Failed to process value " + value, e);
      }
      if (mappedValue != null) {
        mappedValues.put(entry.getKey(), mappedValue);
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, String key, Draft3PortProcessorCallback portProcessor) throws Exception {
    logger.debug("Process value {} and schema {}", value, schema);

    if (value == null) {
      return null;
    }

    Draft3PortProcessorResult portProcessorResult = portProcessor.process(value, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (Draft3SchemaHelper.isAnyFromSchema(schema)) {
      return value;
    }
    
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = Draft3SchemaHelper.getField(entry.getKey(), Draft3SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));

        if (field == null) {
          logger.info("Field {} not found in schema {}", entry.getKey(), schema);
          continue;
        }

        Object singleResult = processValue(entry.getValue(), port, schema, entry.getKey(), portProcessor);
        result.put(entry.getKey(), singleResult);
      }
      return result;
    }

    if (value instanceof List<?>) {
      List<Object> result = new LinkedList<>();

      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = Draft3SchemaHelper.getSchemaForArrayItem(job.getApp().getSchemaDefs(), schema);
        Object singleResult = processValue(item, port, arrayItemSchema, key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
}