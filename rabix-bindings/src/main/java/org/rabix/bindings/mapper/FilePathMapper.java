package org.rabix.bindings.mapper;

import java.util.Map;

/**
 * Maps file path from one format to another 
 */
public interface FilePathMapper {

  /**
   * Map file path
   */
  String map(String path, Map<String, Object> config) throws FileMappingException;

}
