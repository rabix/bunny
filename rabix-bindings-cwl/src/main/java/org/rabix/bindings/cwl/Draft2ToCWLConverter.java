package org.rabix.bindings.cwl;

import org.rabix.bindings.cwl.helper.CWLSchemaHelper;

public class Draft2ToCWLConverter {

  public static String convertStepID(String id) {
    if (id.startsWith("#")) {
      return CWLSchemaHelper.getLastInputId(id.substring(1));
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
    return source.replaceAll("\\.", "/").replaceAll("^\"|\"$", "");
  }

  public static String convertDestinationId(String destination) {
    return CWLSchemaHelper.getLastInputId(destination);
  }
}
