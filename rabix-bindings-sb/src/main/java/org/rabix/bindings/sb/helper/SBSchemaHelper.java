package org.rabix.bindings.sb.helper;

import java.util.*;

import org.rabix.bindings.model.DataType;
import org.rabix.common.helper.CloneHelper;

import com.google.common.base.Preconditions;

public class SBSchemaHelper extends SBBeanHelper {

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
          if (subschema == null || SCHEMA_NULL.equals(subschema)) {
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

  @SuppressWarnings("unchecked")
  public static DataType readDataType(Object schema) {

    // UNION
    if (schema instanceof List) {
      List<?> schemaList = (List<?>) schema;
      int numberOfTypes = schemaList.size() - (schemaList.contains("null") ? 1 : 0);
      if (numberOfTypes > 1 ) {
        Set<DataType> types = new HashSet<>();
        for (Object subschema : schemaList) {
          types.add(readDataType(subschema));
        }
        return new DataType(DataType.Type.UNION, types);
      }
    }

    // FILE
    if (isFileFromSchema(schema))
      return new DataType(DataType.Type.FILE);

    //ARRAY
    if (isArrayFromSchema(schema)) {
      DataType arrayType = readDataType(getItems(schema));
      return new DataType(DataType.Type.ARRAY, arrayType);
    }

    // RECORD
    if (isRecordFromSchema(schema)) {
      Map<String, DataType> subTypes = new HashMap<>();
      Object fields = getFields(schema);
      if (fields instanceof Map<?, ?>) {
        Map<String, Object> fieldsMap = (Map<String, Object>) fields;
        for (String key: fieldsMap.keySet()) {
          subTypes.put(key, readDataType(fieldsMap.get(key)));
        }
      }
      return new DataType(DataType.Type.RECORD, subTypes);
    }

    // PRIMITIVES
    if (isTypeFromSchema(schema, "boolean")) {
      return new DataType(DataType.Type.BOOLEAN);
    }
    if (isTypeFromSchema(schema, "string")) {
      return new DataType(DataType.Type.STRING);
    }
    if (isTypeFromSchema(schema, "int")) {
      return new DataType(DataType.Type.INT);
    }
    if (isTypeFromSchema(schema, "long")) {
      return new DataType(DataType.Type.LONG);
    }
    if (isTypeFromSchema(schema, "float")) {
      return new DataType(DataType.Type.FLOAT);
    }
    if (isTypeFromSchema(schema, "double")) {
      return new DataType(DataType.Type.DOUBLE);
    }
    if (isTypeFromSchema(schema, "null")) {
      return new DataType(DataType.Type.NULL);
    }

    return new DataType(DataType.Type.ANY);
  }


  public static DataType getDataTypeFromValue(Object value) {
    if (value==null)
      return new DataType(DataType.Type.ANY);

    // FILE
    if (isFileFromValue(value))
      return new DataType(DataType.Type.FILE);

    //ARRAY
    if (value instanceof List) {
      DataType arrayType = getDataTypeFromValue(((List<?>)value).get(0));
      return new DataType(DataType.Type.ARRAY, arrayType);
    }

    // RECORD
    if (value instanceof Map) {
      Map<String, DataType> subTypes = new HashMap<>();
      Map<?, ?> valueMap = (Map<?, ?>) value;
      for (Object key: valueMap.keySet()) {
        subTypes.put((String)key, getDataTypeFromValue(valueMap.get(key)));
      }
      return new DataType(DataType.Type.RECORD, subTypes);
    }

    // PRIMITIVE
    for (DataType.Type t : DataType.Type.values()) {
      if (t.primitiveType !=null && t.primitiveType.isInstance(value))
        return new DataType(t);
    }

    return new DataType(DataType.Type.ANY);
  }
}
