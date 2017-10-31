package org.rabix.bindings.draft2.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.draft2.bean.Draft2InputPort;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2OutputPort;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft2PortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(Draft2PortProcessor.class);
  
  private Draft2Job job;
  
  public Draft2PortProcessor(Draft2Job job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, Draft2PortProcessorCallback portProcessor) throws Draft2PortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), Draft2InputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, Draft2PortProcessorCallback portProcessor) throws Draft2PortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), Draft2OutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, Draft2PortProcessorCallback portProcessor) throws Draft2PortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(Draft2SchemaHelper.denormalizeId(id), clazz);
      if (port != null) {
        Object mappedValue = null;
        try {
          mappedValue = processValue(value, port, port.getSchema(), Draft2SchemaHelper.denormalizeId(id), portProcessor);
        } catch (Exception e) {
          throw new Draft2PortProcessorException("Failed to process value " + value, e);
        }
        if (mappedValue != null) {
          mappedValues.put(entry.getKey(), mappedValue);
        }
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, String key, Draft2PortProcessorCallback portProcessor) throws Exception {
    if (value == null) {
      return null;
    }

    Draft2PortProcessorResult portProcessorResult = portProcessor.process(value, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = Draft2SchemaHelper.getField(entry.getKey(), Draft2SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));

        if (field == null  && Draft2SchemaHelper.getType(schema).equals("record")) {
          logger.info("Field {} not found in schema {}", entry.getKey(), schema);
          continue;
        }

        Object singleResult = processValue(entry.getValue(), port, Draft2SchemaHelper.getType(field), entry.getKey(), portProcessor);
        result.put(entry.getKey(), singleResult);
      }
      return result;
    }

    if (value instanceof List<?>) {
      List<Object> result = new LinkedList<>();

      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = Draft2SchemaHelper.getSchemaForArrayItem(item, job.getApp().getSchemaDefs(), schema);
        Object singleResult = processValue(item, port, arrayItemSchema, key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
}
