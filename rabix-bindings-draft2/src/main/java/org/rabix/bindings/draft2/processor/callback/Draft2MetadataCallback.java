package org.rabix.bindings.draft2.processor.callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft2MetadataCallback implements Draft2PortProcessorCallback {

  private final static Logger logger = LoggerFactory.getLogger(Draft2MetadataCallback.class);
  
  private Map<String, Map<String, Object>> pathToMetadata = new HashMap<>();
  private Map<String, Map<String, Object>> outputMetadata = new HashMap<>();
  
  public Draft2MetadataCallback(Map<String, Object> inputs) {
    mapPathsToMetadata(inputs);
  }
  
  /**
   * Extract paths from unknown data
   */
  public void mapPathsToMetadata(Object value) {
    if (value == null) {
      return;
    } else if (Draft2SchemaHelper.isFileFromValue(value)) {
      String path = Draft2FileValueHelper.getPath(value);
      String originalPath = Draft2FileValueHelper.getOriginalPath(value);
      logger.debug("Putting metadata for file {}", path);
      if (!pathToMetadata.containsKey(path)) {
        pathToMetadata.put(path, Draft2FileValueHelper.getMetadata(value));
        if(originalPath != null) {
          pathToMetadata.put(originalPath, Draft2FileValueHelper.getMetadata(value));
        }
      }

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Object subvalue : secondaryFiles) {
          String subpath = Draft2FileValueHelper.getPath(subvalue);
          String suboriginalPath = Draft2FileValueHelper.getOriginalPath(subvalue);
          if (!pathToMetadata.containsKey(subpath)) {
            pathToMetadata.put(subpath, Draft2FileValueHelper.getMetadata(subvalue));
            if(suboriginalPath != null) {
              pathToMetadata.put(suboriginalPath, Draft2FileValueHelper.getMetadata(subvalue));
            }
          }
        }
      }
    } else if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        mapPathsToMetadata(subvalue);
      }
    } else if (value instanceof Map<?, ?>) {
      for (Object subvalue : ((Map<?, ?>) value).values()) {
        mapPathsToMetadata(subvalue);
      }
    }
  }
  
  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      String path = Draft2FileValueHelper.getPath(clonedValue);
      logger.debug("Searching for file {} in pathToMetadata {}.", path, pathToMetadata);
      if (pathToMetadata.containsKey(path)) {
        logger.debug("Output file {} is found in the inputs section.", path);
        
        Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(clonedValue);
        Map<String, Object> newMetadata = pathToMetadata.get(path);
        if (metadata != null) {
          if (newMetadata == null) {
            newMetadata = metadata;
          } else {
            newMetadata.putAll(metadata);
          }
          newMetadata.putAll(metadata);
        }
        Draft2FileValueHelper.setMetadata(newMetadata, clonedValue);
        logger.debug("Combined metadata for file {} is {}.", path, newMetadata);
      }
      outputMetadata.put(path, Draft2FileValueHelper.getMetadata(clonedValue));

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String subpath = Draft2FileValueHelper.getPath(secondaryFileValue);
          
          if (pathToMetadata.containsKey(subpath)) {
            logger.debug("Output file {} is found in the inputs section.", subpath);
            
            Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(secondaryFileValue);
            Map<String, Object> newMetadata = pathToMetadata.get(path);
            if (metadata != null) {
              if (newMetadata == null) {
                newMetadata = metadata;
              } else {
                newMetadata.putAll(metadata);
              }
            }
            Draft2FileValueHelper.setMetadata(newMetadata, secondaryFileValue);
            logger.debug("Combined metadata for file {} is {}.", subpath, newMetadata);
          }
          Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(secondaryFileValue);
          if ((metadata == null || metadata.isEmpty()) && outputMetadata.containsKey(subpath)) {
            Draft2FileValueHelper.setMetadata(outputMetadata.get(subpath), secondaryFileValue);
          }
        }
      }
      return new Draft2PortProcessorResult(clonedValue, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }
  
}
