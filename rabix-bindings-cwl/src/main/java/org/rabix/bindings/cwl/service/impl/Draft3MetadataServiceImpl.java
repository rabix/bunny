package org.rabix.bindings.draft3.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.service.Draft3MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft3MetadataServiceImpl implements Draft3MetadataService {

  private final static Logger logger = LoggerFactory.getLogger(Draft3MetadataServiceImpl.class);
  
  /**
   * Process metadata inheritance
   */
  public Map<String, Object> processMetadata(Draft3Job job, Object value, Draft3OutputPort outputPort, Object outputBinding) {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    
    Map<String, Object> metadata = Draft3FileValueHelper.getMetadata(value);

    String inputId = Draft3BindingHelper.getInheritMetadataFrom(outputBinding);
    if (StringUtils.isEmpty(inputId)) {
      logger.info("Metadata for {} is {}.", outputPort.getId(), metadata);
      return metadata;
    }

    Object input = null;
    String normalizedInputId = Draft3SchemaHelper.normalizeId(inputId);
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

    if (Draft3SchemaHelper.isFileFromValue(input)) {
      Map<String, Object> metadata = Draft3FileValueHelper.getMetadata(input);
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
  public Object evaluateMetadataExpressions(Draft3Job job, Object self, Object metadata) throws Draft3ExpressionException {
    if (metadata == null) {
      return null;
    }
    Object result = Draft3ExpressionResolver.resolve(metadata, job, self);
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
