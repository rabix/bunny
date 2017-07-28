package org.rabix.bindings.draft2.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.DataType;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Draft2SchemaHelper extends Draft2BeanHelper {

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
  public static final String KEY_SCHEMA_SYMBOLS = "symbols";

  public static final String KEY_JOB_TYPE = "class";

  public static final String TYPE_JOB_FILE = "File";
  public static final String TYPE_JOB_EXPRESSION = "Expression";
  public static final String TYPE_JOB_ARRAY = "array";
  public static final String TYPE_JOB_RECORD = "record";

  public static final String SCHEMA_NULL = "null";

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

  public static Object getItems(Object raw) {
    return getValue(KEY_SCHEMA_ITEMS, raw);
  }

  public static String getName(Object raw) {
    return getValue(KEY_SCHEMA_NAME, raw);
  }

  public static Object getInputBinding(Object raw) {
    if(raw instanceof List) {
      for(Object elem: (List<?>) raw) {
        if(elem != null && elem instanceof Map) {
          return getValue(KEY_INPUT_BINDING_ADAPTER, elem);
        }
      }
    }
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
    return isTypeFromSchema(schema, TYPE_JOB_ARRAY);
  }

  public static boolean isRecordFromSchema(Object schema) {
    return isTypeFromSchema(schema, TYPE_JOB_RECORD);
  }

  @SuppressWarnings("unchecked")
  public static boolean isRequired(Object schema) {
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
    
    if (recordSchema instanceof Map<?, ?>) {
      return recordSchema;
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
  public static Object getSchemaForArrayItem(Object value, List<Map<String, Object>> schemaDefs, Object arraySchema) {
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

    List<Object> schemaObjects = new ArrayList<>();
    for (Object schema : schemas) {
      Object schemaObj = findSchema(schemaDefs, schema);

      if (schemaObj == null) {
        continue;
      }
      schemaObjects.add(schemaObj);
    }
    if (schemaObjects.size() == 1) {
      return schemaObjects.get(0);
    }
    if (schemaObjects.size() > 1) {
      for (Object schemaObj : schemaObjects) {
        if (validateAvro(JSONHelper.writeObject(value), JSONHelper.writeObject(schemaObj))) {
          return schemaObj;
        }
      }
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

  public static boolean validateAvro(String json, String schemaStr) {
    Schema schema = new Schema.Parser().parse(schemaStr);

    List<Schema> schemas = new ArrayList<>();
    schemas.add(schema);
    try {
      resolveUnion(JSONHelper.readJsonNode(json), schemas);
      return true;
    } catch (BindingException e) {
      return false;
    }
  }

  private static Schema resolveUnion(JsonNode datum, Collection<Schema> schemas) throws BindingException {
    Set<Schema.Type> primitives = Sets.newHashSet();
    List<Schema> others = Lists.newArrayList();
    for (Schema schema : schemas) {
      if (PRIMITIVES.containsKey(schema.getType())) {
        primitives.add(schema.getType());
      } else {
        others.add(schema);
      }
    }

    // Try to identify specific primitive types
    Schema primitiveSchema = null;
    if (datum == null || datum.isNull()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.NULL);
    } else if (datum.isShort() || datum.isInt()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.INT, Schema.Type.LONG, Schema.Type.FLOAT,
          Schema.Type.DOUBLE);
    } else if (datum.isLong()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.LONG, Schema.Type.DOUBLE);
    } else if (datum.isFloat()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.FLOAT, Schema.Type.DOUBLE);
    } else if (datum.isDouble()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.DOUBLE);
    } else if (datum.isBoolean()) {
      primitiveSchema = closestPrimitive(primitives, Schema.Type.BOOLEAN);
    }

    if (primitiveSchema != null) {
      return primitiveSchema;
    }

    // otherwise, select the first schema that matches the datum
    for (Schema schema : others) {
      if (matches(datum, schema)) {
        return schema;
      }
    }

    throw new BindingException(String.format("Cannot resolve union: %s not in %s", datum, schemas));
  }

  // this does not contain string, bytes, or fixed because the datum type
  // doesn't necessarily determine the schema.
  private static ImmutableMap<Schema.Type, Schema> PRIMITIVES = ImmutableMap.<Schema.Type, Schema> builder()
      .put(Schema.Type.NULL, Schema.create(Schema.Type.NULL))
      .put(Schema.Type.BOOLEAN, Schema.create(Schema.Type.BOOLEAN)).put(Schema.Type.INT, Schema.create(Schema.Type.INT))
      .put(Schema.Type.LONG, Schema.create(Schema.Type.LONG)).put(Schema.Type.FLOAT, Schema.create(Schema.Type.FLOAT))
      .put(Schema.Type.DOUBLE, Schema.create(Schema.Type.DOUBLE)).build();

  private static Schema closestPrimitive(Set<Schema.Type> possible, Schema.Type... types) {
    for (Schema.Type type : types) {
      if (possible.contains(type) && PRIMITIVES.containsKey(type)) {
        return PRIMITIVES.get(type);
      }
    }
    return null;
  }

  private static boolean matches(JsonNode datum, Schema schema) throws BindingException {
    switch (schema.getType()) {
    case RECORD:
      if (datum.isObject()) {
        // check that each field is present or has a default
        for (Schema.Field field : schema.getFields()) {
          JsonNode toValidate = null;
          if (!datum.has(field.name()) && field.defaultValue() == null) {
            toValidate = null;
          } else {
            toValidate = datum.get(field.name());
          }
          List<Schema> schemas = new ArrayList<>();
          schemas.add(field.schema());
          resolveUnion(toValidate, schemas);
        }
        return true;
      }
      break;
    case UNION:
      if (resolveUnion(datum, schema.getTypes()) != null) {
        return true;
      }
      break;
    case MAP:
      if (datum.isObject()) {
        return true;
      }
      break;
    case ARRAY:
      if (datum.isArray()) {
        return true;
      }
      break;
    case BOOLEAN:
      if (datum.isBoolean()) {
        return true;
      }
      break;
    case FLOAT:
      if (datum.isFloat() || datum.isInt()) {
        return true;
      }
      break;
    case DOUBLE:
      if (datum.isDouble() || datum.isFloat() || datum.isLong() || datum.isInt()) {
        return true;
      }
      break;
    case INT:
      if (datum.isInt()) {
        return true;
      }
      break;
    case LONG:
      if (datum.isLong() || datum.isInt()) {
        return true;
      }
      break;
    case STRING:
      if (datum.isTextual()) {
        return true;
      }
      break;
    case ENUM:
      if (datum.isTextual() && schema.hasEnumSymbol(datum.textValue())) {
        return true;
      }
      break;
    case BYTES:
    case FIXED:
      if (datum.isBinary()) {
        return true;
      }
      break;
    case NULL:
      if (datum == null || datum.isNull()) {
        return true;
      }
      break;
    default: // UNION or unknown
      throw new IllegalArgumentException("Unsupported schema: " + schema);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static DataType readDataType(Object schema) {

    // UNION
    if (schema instanceof List) {
      List<?> schemaList = (List<?>) schema;
      if (schemaList.size() == 1) {
        schema = schemaList.get(0);
      } else {
        int numberOfTypes = schemaList.size() - (schemaList.contains(SCHEMA_NULL) ? 1 : 0);
        if (numberOfTypes > 1) {
          Set<DataType> types = new HashSet<>();
          for (Object subschema : schemaList) {
            types.add(readDataType(subschema));
          }
          return new DataType(DataType.Type.UNION, types, !isRequired(schema));
        }
      }
    }

    // FILE
    if (isFileFromSchema(schema))
      return new DataType(DataType.Type.FILE, !isRequired(schema));

    //ARRAY
    if (isArrayFromSchema(schema)) {
      DataType arrayType = readDataType(getItems(schema));
      return new DataType(DataType.Type.ARRAY, arrayType, !isRequired(schema));
    }

    // ENUM
    if (isTypeFromSchema(schema, "enum")) {
      Object symbols = getValue(KEY_SCHEMA_SYMBOLS, schema);
      if (symbols == null || !(symbols instanceof List))
        return new DataType(DataType.Type.ANY);
      List<String> list = new ArrayList<>();
      for (Object o: (List<?>) symbols) {
        if (!(o instanceof String))
          return new DataType(DataType.Type.ANY);
        list.add((String) o);
      }
      return new DataType(DataType.Type.ENUM, list, !isRequired(schema));
    }

    // RECORD
    if (isRecordFromSchema(schema)) {
      Map<String, DataType> subTypes = new HashMap<>();
      Object fields = getFields(schema);
      if (fields instanceof List<?>) {
        for (Object o : (List<Object>) fields) {
          Map<String, Object> map = (Map<String, Object>) o;
          subTypes.put((String) map.get("name"), readDataType(map.get("type")));
        }
      }
      return new DataType(DataType.Type.RECORD, subTypes, !isRequired(schema));
    }
    // MAP
    if (isTypeFromSchema(schema, "map")) {
      DataType mapType = readDataType(getValue("values", schema));
      return new DataType(DataType.Type.MAP, mapType, !isRequired(schema));
    }

    // PRIMITIVES
    if (isTypeFromSchema(schema, "boolean")) {
      return new DataType(DataType.Type.BOOLEAN, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, "string")) {
      return new DataType(DataType.Type.STRING, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, "int")) {
      return new DataType(DataType.Type.INT, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, "long")) {
      return new DataType(DataType.Type.INT, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, "float")) {
      return new DataType(DataType.Type.FLOAT, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, "double")) {
      return new DataType(DataType.Type.FLOAT, !isRequired(schema));
    }
    if (isTypeFromSchema(schema, SCHEMA_NULL)) {
      return new DataType(DataType.Type.NULL);
    }

    return new DataType(DataType.Type.ANY);
  }

}