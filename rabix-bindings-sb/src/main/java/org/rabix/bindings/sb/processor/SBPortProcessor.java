package org.rabix.bindings.sb.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBOutputPort;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SBPortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(SBPortProcessor.class);
  
  private SBJob job;
  
  public SBPortProcessor(SBJob job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, SBPortProcessorCallback portProcessor) throws SBPortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), SBInputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, SBPortProcessorCallback portProcessor) throws SBPortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), SBOutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, SBPortProcessorCallback portProcessor) throws SBPortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = SBSchemaHelper.denormalizeId(entry.getKey());
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(id, clazz);
      if (port == null) {
        throw new SBPortProcessorException("Port with ID=" + SBSchemaHelper.denormalizeId(id) + " doesn't exist.");
      }
      Object mappedValue = null;
      try {
        mappedValue = processValue(value, port, port.getSchema(), port.getBinding(), id, portProcessor);
      } catch (Exception e) {
        throw new SBPortProcessorException("Failed to process value " + value, e);
      }
      if (mappedValue != null) {
        mappedValues.put(entry.getKey(), mappedValue);
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, Object binding, String key, SBPortProcessorCallback portProcessor) throws Exception {
    logger.debug("Process value {} and schema {}", value, schema);

    if (value == null) {
      return null;
    }

    SBPortProcessorResult portProcessorResult = portProcessor.process(value, key, schema, binding, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (SBSchemaHelper.isFileFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = SBSchemaHelper.getField(entry.getKey(), SBSchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));
        
        if (field == null && SBSchemaHelper.getType(schema).equals("record")) {
          logger.info("Field {} not found in schema {}", entry.getKey(), schema);
          continue;
        }

        Object fieldBinding = port instanceof SBInputPort ? SBSchemaHelper.getInputBinding(field) : SBSchemaHelper.getOutputBinding(field);
        Object singleResult = processValue(entry.getValue(), port, SBSchemaHelper.getType(field), fieldBinding, entry.getKey(), portProcessor);
        result.put(entry.getKey(), singleResult);
      }
      return result;
    }

    if (value instanceof List<?>) {
      List<Object> result = new LinkedList<>();

      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = SBSchemaHelper.getSchemaForArrayItem(job.getApp().getSchemaDefs(), schema);
        Object singleResult = processValue(item, port, arrayItemSchema, port.getBinding(), key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
}