package org.rabix.bindings.cwl.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWLPortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(CWLPortProcessor.class);
  
  private CWLJob job;
  
  public CWLPortProcessor(CWLJob job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, CWLPortProcessorCallback portProcessor) throws CWLPortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), CWLInputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, CWLPortProcessorCallback portProcessor) throws CWLPortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), CWLOutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, CWLPortProcessorCallback portProcessor) throws CWLPortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(CWLSchemaHelper.normalizeId(id), clazz);
      if (port != null) {
        Object mappedValue = null;
        try {
          mappedValue = processValue(value, port, port.getSchema(), port.getBinding(), CWLSchemaHelper.normalizeId(id), portProcessor);
        } catch (Exception e) {
          throw new CWLPortProcessorException("Failed to process value " + value, e);
        }
        mappedValues.put(entry.getKey(), mappedValue);
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, Object binding, String key, CWLPortProcessorCallback portProcessor) throws Exception {
    logger.debug("Process value {} and schema {}", value, schema);

    if (value == null) {
      return null;
    }

    CWLPortProcessorResult portProcessorResult = portProcessor.process(value, key, schema, binding, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (CWLSchemaHelper.isAnyFromSchema(schema)) {
      return value;
    }
    
    if (CWLSchemaHelper.isFileFromValue(value)) {
      return value;
    }
    
    if (CWLSchemaHelper.isDirectoryFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = CWLSchemaHelper.getField(entry.getKey(), CWLSchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));

        if (field == null && CWLSchemaHelper.getType(schema).equals("record")) {
          logger.info("Field {} not found in schema {}", entry.getKey(), schema);
          continue;
        }

        Object fieldBinding = port instanceof CWLInputPort ? CWLSchemaHelper.getInputBinding(field) : CWLSchemaHelper.getOutputBinding(field);
        Object singleResult = processValue(entry.getValue(), port, CWLSchemaHelper.getType(field), fieldBinding == null ? binding : fieldBinding, entry.getKey(), portProcessor);
        result.put(entry.getKey(), singleResult);
      }
      return result;
    }

    if (value instanceof List<?>) {
      List<Object> result = new LinkedList<>();

      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = CWLSchemaHelper.getSchemaForArrayItem(item, job.getApp().getSchemaDefs(), schema);
        Object fieldBinding = port instanceof CWLInputPort ? CWLSchemaHelper.getInputBinding(item) : CWLSchemaHelper.getOutputBinding(item);
        Object singleResult = processValue(item, port, arrayItemSchema, fieldBinding == null ? binding : fieldBinding, key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
  
  public CWLJob getJob() {
    return job;
  }
}