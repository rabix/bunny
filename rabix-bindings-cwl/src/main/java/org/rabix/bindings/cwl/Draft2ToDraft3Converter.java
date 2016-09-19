package org.rabix.bindings.draft3;

import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;

public class Draft2ToDraft3Converter {

  public static String convertStepID(String id) {
    if (id.startsWith("#")) {
      return Draft3SchemaHelper.getLastInputId(id.substring(1));
    }
    return id;
  }
  
  public static String convertPortID(String id) {
    if (id.startsWith("#")) {
      return id.substring(1);
    }
    return id;
  }
  
  public static String convertSource(String source) {
    return source.replaceAll("\\.", "/");
  }

  public static String convertDestinationId(String destination) {
    return Draft3SchemaHelper.getLastInputId(destination);
  }
}
