package org.rabix.bindings.sb.processor.callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.CloneHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SBMetadataCallback implements SBPortProcessorCallback {

  private final static Logger logger = LoggerFactory.getLogger(SBMetadataCallback.class);
  
  private Map<String, Map<String, Object>> pathToMetadata = new HashMap<>();
  private Map<String, Map<String, Object>> outputMetadata = new HashMap<>();
  
  public SBMetadataCallback(Map<String, Object> inputs) {
    mapPathsToMetadata(inputs);
  }
  
  /**
   * Extract paths from unknown data
   */
  public void mapPathsToMetadata(Object value) {
    if (value == null) {
      return;
    } else if (SBSchemaHelper.isFileFromValue(value)) {
      String path = SBFileValueHelper.getPath(value);
      String originalPath = SBFileValueHelper.getOriginalPath(value);
      logger.debug("Putting metadata for file {}", path);
      if (!pathToMetadata.containsKey(path)) {
        pathToMetadata.put(path, SBFileValueHelper.getMetadata(value));
        if(originalPath != null) {
          pathToMetadata.put(originalPath, SBFileValueHelper.getMetadata(value));
        }
      }

      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Object subvalue : secondaryFiles) {
          String subpath = SBFileValueHelper.getPath(subvalue);
          String suboriginalPath = SBFileValueHelper.getOriginalPath(subvalue);
          if (!pathToMetadata.containsKey(subpath)) {
            pathToMetadata.put(subpath, SBFileValueHelper.getMetadata(subvalue));
            if(suboriginalPath != null) {
              pathToMetadata.put(suboriginalPath, SBFileValueHelper.getMetadata(subvalue));
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
  public SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      String path = SBFileValueHelper.getPath(clonedValue);
      logger.debug("Searching for file {} in pathToMetadata {}.", path, pathToMetadata);
      if (pathToMetadata.containsKey(path)) {
        logger.debug("Output file {} is found in the inputs section.", path);
        
        Map<String, Object> metadata = SBFileValueHelper.getMetadata(clonedValue);
        Map<String, Object> newMetadata = pathToMetadata.get(path);
        if (metadata != null) {
          if (newMetadata == null) {
            newMetadata = metadata;
          } else {
            newMetadata.putAll(metadata);
          }
        }
        SBFileValueHelper.setMetadata(newMetadata, clonedValue);
        logger.debug("Combined metadata for file {} is {}.", path, newMetadata);
      }
      outputMetadata.put(path, SBFileValueHelper.getMetadata(clonedValue));

      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String subpath = SBFileValueHelper.getPath(secondaryFileValue);
          
          if (pathToMetadata.containsKey(subpath)) {
            logger.debug("Output file {} is found in the inputs section.", subpath);
            
            Map<String, Object> metadata = SBFileValueHelper.getMetadata(secondaryFileValue);
            Map<String, Object> newMetadata = pathToMetadata.get(path);
            if (metadata != null) {
              if (newMetadata == null) {
                newMetadata = metadata;
              } else {
                newMetadata.putAll(metadata);
              }
            }
            SBFileValueHelper.setMetadata(newMetadata, secondaryFileValue);
            logger.debug("Combined metadata for file {} is {}.", subpath, newMetadata);
          }
          Map<String, Object> metadata = SBFileValueHelper.getMetadata(secondaryFileValue);
          if ((metadata == null || metadata.isEmpty()) && outputMetadata.containsKey(subpath)) {
            SBFileValueHelper.setMetadata(outputMetadata.get(subpath), secondaryFileValue);
          }
        }
      }
      return new SBPortProcessorResult(clonedValue, true);
    }
    return new SBPortProcessorResult(value, false);
  }
  
}
