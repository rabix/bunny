package org.rabix.engine;

import java.util.UUID;

public class SchemaHelper {

  public static UUID toUUID(String value) {
    return value!=null? UUID.fromString(value) : null;
  }
  
  public static String fromUUID(UUID value) {
    return value!=null? value.toString() : null;
  }
  
}
