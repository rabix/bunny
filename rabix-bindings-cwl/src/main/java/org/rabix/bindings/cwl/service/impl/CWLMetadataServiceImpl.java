package org.rabix.bindings.cwl.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.service.CWLMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWLMetadataServiceImpl implements CWLMetadataService {

  private final static Logger logger = LoggerFactory.getLogger(CWLMetadataServiceImpl.class);
  
  /**
   * Process metadata inheritance
   */
  public Map<String, Object> processMetadata(CWLJob job, Object value, CWLOutputPort outputPort, Object outputBinding) {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    
    Map<String, Object> metadata = CWLFileValueHelper.getMetadata(value);

    String inputId = CWLBindingHelper.getInheritMetadataFrom(outputBinding);
    if (StringUtils.isEmpty(inputId)) {
      logger.info("Metadata for {} is {}.", outputPort.getId(), metadata);
      return metadata;
    }

    Object input = null;
    String normalizedInputId = CWLSchemaHelper.normalizeId(inputId);
    for (Entry<String, Object> inputEntry : job.getInputs().entrySet()) {
      if (inputEntry.getKey().equals(normalizedInputId)) {
        input = inputEntry.getValue();
        break;
      }
    }

    List<Map<String, Object>> metadataList = findAllMetadata(input);
    Map<String, Object> inheritedMetadata = intersect(metadataList);
    if (inheritedMetadata == null) {
      return metadata;
    }

    if (metadata != null) {
      inheritedMetadata.putAll(metadata);
    }
    logger.info("Metadata for {} is {}.", outputPort.getId(), inheritedMetadata);
    return inheritedMetadata;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> findAllMetadata(Object input) {
    if (input == null) {
      return null;
    }
    List<Map<String, Object>> result = new ArrayList<>();

    if (CWLSchemaHelper.isFileFromValue(input)) {
      Map<String, Object> metadata = CWLFileValueHelper.getMetadata(input);
      result.add(metadata != null ? metadata : new HashMap<String, Object>());
    } else if (input instanceof List<?>) {
      for (Object inputPart : ((List<Object>) input)) {
        List<Map<String, Object>> resultPart = findAllMetadata(inputPart);
        if (resultPart != null) {
          result.addAll(resultPart);
        }
      }
    } else if (input instanceof Map<?, ?>) {
      for (Entry<String, Object> inputPartEntry : ((Map<String, Object>) input).entrySet()) {
        List<Map<String, Object>> resultPart = findAllMetadata(inputPartEntry.getValue());
        if (resultPart != null) {
          result.addAll(resultPart);
        }
      }
    }
    return result;
  }

  private Map<String, Object> intersect(List<Map<String, Object>> metadataList) {
    if (metadataList == null || metadataList.isEmpty()) {
      return null;
    }

    Map<String, Object> inheritedMeta = new HashMap<String, Object>();

    Map<String, Object> firstMetaData = metadataList.get(0);
    for (String metaDataKey : firstMetaData.keySet()) {
      Object metaDataValue1 = firstMetaData.get(metaDataKey);

      boolean equals = true;
      for (int i = 1; i < metadataList.size(); i++) {
        Object metaDataValue2 = metadataList.get(i).get(metaDataKey);

        if (!(
            (metaDataValue1 == null && metaDataValue2 == null) // special case (preserve null "value")
            || (metaDataValue1 != null && metaDataValue2 != null && metaDataValue1.equals(metaDataValue2))) // values are the same
            ) {
          equals = false;
          break;
        }

      }
      if (equals) {
        inheritedMeta.put(metaDataKey, metaDataValue1);
      }
    }
    return inheritedMeta;
  }

  @SuppressWarnings("unchecked")
  public Object evaluateMetadataExpressions(CWLJob job, Object self, Object metadata) throws CWLExpressionException {
    if (metadata == null) {
      return null;
    }
    Object result = CWLExpressionResolver.resolve(metadata, job, self);
    if (metadata instanceof Map<?, ?>) {
      result = new HashMap<>();
      for (Entry<String, Object> outputEntry : ((Map<String, Object>) metadata).entrySet()) {
        Object resolved = evaluateMetadataExpressions(job, self, outputEntry.getValue());
        ((Map<String, Object>) result).put(outputEntry.getKey(), resolved);
      }
      return result;
    } else if (metadata instanceof List<?>) {
      result = new ArrayList<>();
      for (Object value : ((List<Object>) metadata)) {
        Object resolvedMetadata = evaluateMetadataExpressions(job, self, value);
        if (resolvedMetadata != null) {
          ((List<Object>) result).add(resolvedMetadata);
        }
      }
    }
    return result;
  }

}
