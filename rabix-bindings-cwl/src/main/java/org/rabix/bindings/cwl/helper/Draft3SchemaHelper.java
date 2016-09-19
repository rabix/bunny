package org.rabix.bindings.draft3.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.common.helper.CloneHelper;

import com.google.common.base.Preconditions;

public class Draft3SchemaHelper extends Draft3BeanHelper {

  public static final String ID_START = "#";
  public static final String ID_SEPARATOR = "/";
  public static final String PORT_ID_SEPARATOR = ".";

  public static final String STEP_PORT_ID = "id";
  
  public static final String KEY_SCHEMA_TYPE = "type";
  public static final String KEY_SCHEMA_NAME = "name";
  public static final String KEY_SCHEMA_ITEMS = "items";
  public static final String KEY_INPUT_BINDING_ADAPTER = "inputBinding";
  public static final String KEY_OUTPUT_BINDING_ADAPTER = "outputBinding";
  public static final String KEY_SCHEMA_FIELDS = "fields";
  
  public static final String KEY_JOB_TYPE = "class";
  
  public static final String TYPE_JOB_FILE = "File";
  public static final String TYPE_JOB_EXPRESSION = "Expression";
  public static final String TYPE_JOB_ARRAY = "array";
  public static final String TYPE_JOB_RECORD = "record";
  public static final String TYPE_JOB_ANY = "Any";
  
  public static final String SCHEMA_NULL = "null";
  
  public static final String OPTIONAL_SHORTENED = "?";
  public static final String ARRAY_SHORTENED = "[]";
  
  public static String normalizeId(String id) {
    if (id == null) {
      return null;
    }
    return id.startsWith(ID_START) ? id.substring(1) : id;
  }
  
  public static String denormalizeId(String id) {
    if (id == null) {
      return null;
    }
    return id.startsWith(ID_START) ? id : ID_START + id;
  }
  
  public static Object getFields(Object raw) {
    return getValue(KEY_SCHEMA_FIELDS, raw);
  }
  
  public static Object getItems(Object schema) {
    String shortenedSchema = getArrayShortenedType(schema);
    if (shortenedSchema != null) {
      return shortenedSchema;
    }
    return getValue(KEY_SCHEMA_ITEMS, schema);
  }
  
  public static String getName(Object raw) {
    return getValue(KEY_SCHEMA_NAME, raw);
  }
  
  public static Object getInputBinding(Object raw) {
    return getValue(KEY_INPUT_BINDING_ADAPTER, raw);
  }
  
  public static Object getOutputBinding(Object raw) {
    return getValue(KEY_OUTPUT_BINDING_ADAPTER, raw);
  }
  
  public static Object getType(Object raw) {
    return getValue(KEY_SCHEMA_TYPE, raw);
  }
  
  public static boolean isFileFromSchema(Object schema) {
    return isTypeFromSchema(schema, TYPE_JOB_FILE);
  }

  public static boolean isArrayFromSchema(Object schema) {
    String shortenedSchema = getArrayShortenedType(schema);
    if (shortenedSchema != null) {
      return true;
    }
    return isTypeFromSchema(schema, TYPE_JOB_ARRAY);
  }
  
  public static boolean isAnyFromSchema(Object schema) {
    return isTypeFromSchema(schema, TYPE_JOB_ANY);
  }

  public static boolean isRecordFromSchema(Object schema) {
    return isTypeFromSchema(schema, TYPE_JOB_RECORD);
  }
  
  @SuppressWarnings("unchecked")
  public static boolean isRequired(Object schema) {
    String shortenedSchema = getOptionalShortenedType(schema);
    if (shortenedSchema != null) {
      return false;
    }
    try {
      Object clonedSchema = CloneHelper.deepCopy(schema);
      while (clonedSchema instanceof Map<?, ?> && ((Map<?, ?>) clonedSchema).containsKey("type")) {
        clonedSchema = ((Map<?, ?>) clonedSchema).get("type");
      }
      if (clonedSchema instanceof List<?>) {
        for (Object subschema : ((List<Object>) clonedSchema)) {
          if (subschema == null) {
            return false;
          }
        }
        return true;
      }
      return clonedSchema != null;
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone schema " + schema);
    }
  }
  
  private static String getOptionalShortenedType(Object schema) {
    if (schema == null) {
      return null;
    }
    if (!(schema instanceof String)) {
      return null;
    }
    String schemaStr = ((String) schema).trim();
    if (schemaStr.endsWith(OPTIONAL_SHORTENED)) {
      return schemaStr.substring(0, schemaStr.length() - 1);
    }
    return null;
  }
  
  private static String getArrayShortenedType(Object schema) {
    if (schema == null) {
      return null;
    }
    if (!(schema instanceof String)) {
      return null;
    }
    String schemaStr = ((String) schema).trim();
    String optionalShortenedType = getOptionalShortenedType(schemaStr);
    if (optionalShortenedType != null) {
      schemaStr = optionalShortenedType;
    }
    if (schemaStr.endsWith(ARRAY_SHORTENED)) {
      return schemaStr.substring(0, schemaStr.length() - ARRAY_SHORTENED.length());
    }
    return null;
  }

  
  @SuppressWarnings("unchecked")
  private static boolean isTypeFromSchema(Object schema, String type) {
    Preconditions.checkNotNull(type);

    if (schema == null) {
      return false;
    }
    if (type.equals(schema)) {
      return true;
    }
    if (schema instanceof Map<?, ?>) {
      Map<String, Object> schemaMap = (Map<String, Object>) schema;
      if (schemaMap.containsKey(KEY_SCHEMA_TYPE)) {
        return type.equals(schemaMap.get(KEY_SCHEMA_TYPE));
      }
    }
    if (schema instanceof List<?>) {
      List<?> schemaList = (List<?>) schema;
      for (Object subschema : schemaList) {
        boolean isType = isTypeFromSchema(subschema, type);
        if (isType) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static boolean isFileFromValue(Object valueObj) {
    if (valueObj == null) {
      return false;
    }
    if (valueObj instanceof Map<?, ?>) {
      Map<?, ?> valueMap = (Map<?, ?>) valueObj;
      Object type = valueMap.get(KEY_JOB_TYPE);

      if (type != null && type.equals(TYPE_JOB_FILE)) {
        return true;
      }
    }
    return false;
  }

  public static Map<?, ?> getField(String field, Object schema) {
    Object fields = getFields(schema);

    Object fieldObj = null;
    if (fields instanceof Map<?, ?>) {
      fieldObj = ((Map<?, ?>) fields).get(field);
    } else if (fields instanceof List<?>) {
      for (Object tmpField : ((List<?>) fields)) {
        if (field.equals(getName(tmpField))) {
          fieldObj = tmpField;
          break;
        }
      }
    }
    return (Map<?, ?>) fieldObj;
  }

  public static Object findSchema(List<Map<String, Object>> schemaDefs, Object schema) {
    if (schema == null || TYPE_JOB_FILE.equals(schema)) {
      return schema;
    }
    if (schema instanceof Map) {
      return schema;
    }
    if (schema instanceof List) {
      return new HashMap<>();
    }

    return getSchemaDef(schemaDefs, (String) schema);
  }

  /**
   * Extract schema from schema definitions
   * 
   * TODO implement AVRO validation for multiple matches
   */
  private static Object getSchemaDef(List<Map<String, Object>> schemaDefs, String name) {
    if (schemaDefs == null) {
      return null;
    }

    List<Object> compatibleSchemas = new ArrayList<>();
    for (Map<String, Object> schemaDef : schemaDefs) {
      if (name.equals(getName(schemaDef))) {
        compatibleSchemas.add(schemaDef);
      }
    }
    if (compatibleSchemas.size() == 1) {
      return compatibleSchemas.get(0);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static Object getSchemaForRecordField(List<Map<String, Object>> schemaDefs, Object recordSchema) {
    if (recordSchema == null) {
      return null;
    }

    List<Object> recordSchemaList = null;
    if (recordSchema instanceof List<?>) {
      recordSchemaList = (List<Object>) recordSchema;
    } else {
      recordSchemaList = new ArrayList<>();
      recordSchemaList.add(recordSchema);
    }
    
    for (Object recordSchemaItem : recordSchemaList) {
      Object schemaObj = findSchema(schemaDefs, recordSchemaItem);
      if (schemaObj == null) {
        continue;
      }
      
      return schemaObj;
    }
    return new HashMap<>();
  }
  
  @SuppressWarnings("unchecked")
  public static Object getSchemaForArrayItem(List<Map<String, Object>> schemaDefs, Object arraySchema) {
    String shortenedSchema = getArrayShortenedType(arraySchema);
    if (shortenedSchema != null) {
      Object shortenedSchemaObj = findSchema(schemaDefs, shortenedSchema);
      if (shortenedSchemaObj != null) {
        return shortenedSchemaObj;
      }
      return shortenedSchema;
    }
    
    if (arraySchema == null) {
      return null;
    }

    List<Object> arraySchemaList = null;
    if (arraySchema instanceof List<?>) {
      arraySchemaList = (List<Object>) arraySchema;
    } else {
      arraySchemaList = new ArrayList<>();
      arraySchemaList.add(arraySchema);
    }
    
    List<Object> schemas = new ArrayList<>();
    
    for (Object arraySchemaItem : arraySchemaList) {
      Object itemSchemaObj = getItems(arraySchemaItem);
      if (itemSchemaObj == null) {
        continue;
      }
      if (itemSchemaObj instanceof List) {
        schemas = (List<Object>) itemSchemaObj;
      } else {
        schemas.add(itemSchemaObj);
      }
    }
    
    for (Object schema : schemas) {
      Object schemaObj = findSchema(schemaDefs, schema);

      if (schemaObj == null) {
        continue;
      }
      return schemaObj;
    }
    return new HashMap<>();
  }
  
  public static String getLastInputId(String id) {
    if (id == null) {
      return null;
    }
    if (id.contains(PORT_ID_SEPARATOR)) {
      return id.substring(id.indexOf(PORT_ID_SEPARATOR) + 1);
    }
    return id;
  }

}
