package org.rabix.bindings.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataType {
  public enum Type {
    UNION, ARRAY, RECORD, FILE(null, "File"), DIRECTORY, ANY, NULL(null, "null"),
    BOOLEAN(new Class<?>[] {Boolean.class}, "boolean"), STRING(new Class<?>[] {String.class}, "string"),
    INT(new Class<?>[] {Integer.class, Long.class}, "int"), FLOAT(new Class<?>[] {Float.class, Double.class}, "float");

    public final Class<?>[] primitiveTypes;
    public final String avroType;

    Type() {
      primitiveTypes = null;
      avroType = null;
    }

    Type(Class<?>[] primitiveTypes, String avroType) {
      this.primitiveTypes = primitiveTypes;
      this.avroType = avroType;
    }
    public boolean isPrimitive(Object value) {
      if (primitiveTypes == null)
        return false;
      for (Class<?> c : primitiveTypes) {
        if (c.isInstance(value))
          return true;
      }
      return false;
    }
  }

  @JsonCreator
  public DataType(@JsonProperty("type") String type) {
    this.type = Type.valueOf(type);
  }

  public DataType(Type type) {
    this.type = type;
  }

  public DataType(Type type, Boolean nullable) {
    this.type = type;
    this.nullable = nullable;
  }

  // Constructor for UNION
  public DataType(Type type, Set<DataType> types, Boolean nullable) {
    this.type = type;
    this.types = types;
    this.nullable = nullable;
  }
  public DataType(Type type, Set<DataType> types) {
    this(type, types, null);
  }

  // Constructor for ARRAY
  public DataType(Type type, DataType subtype, Boolean nullable) {
    this.type = type;
    this.subtype = subtype;
    this.nullable = nullable;
  }
  public DataType(Type type, DataType subtype) {
    this(type, subtype, null);
  }

  // Constructor for RECORD
  public DataType(Type type, Map<String, DataType> subtypes, Boolean nullable) {
    this.type = type;
    this.subtypes = subtypes;
    this.nullable = nullable;
  }
  public DataType(Type type, Map<String, DataType> subtypes) {
    this(type, subtypes, null);
  }

  @JsonProperty("type")
  private final Type type;

  @JsonProperty("subtype")
  private DataType subtype;

  @JsonProperty("types")
  private Set<DataType> types;

  @JsonProperty("subtypes")
  private Map<String, DataType> subtypes;

  @JsonProperty("nullable")
  private Boolean nullable;

  public Boolean isNullable() {
    return nullable;
  }

  public Type getType() {
    return type;
  }

  public DataType getSubtype() {
    return subtype;
  }

  public Set<DataType> getTypes() {
    return types;
  }

  public Map<String, DataType> getSubtypes() {
    return subtypes;
  }
  @JsonIgnore
  public boolean isRecord() {
    return type == Type.RECORD;
  }
  @JsonIgnore
  public boolean isArray() {
    return type == Type.ARRAY;
  }
  @JsonIgnore
  public boolean isUnion() {
    return type == Type.UNION;
  }
  @JsonIgnore
  public boolean isFile() {
    return isType(Type.FILE);
  }

  public boolean isType(Type t) {
    if (type == t)
      return true;
    if (!isUnion())
      return false;
    for (DataType dt : types) {
      if (dt.getType() == t)
        return true;
    }
    return false;
  }
  public boolean isCompatible(DataType value) {
    return this.isCompatible(value, true);
  }

  public boolean isCompatible(DataType value, boolean allowAny) {
    if (value == null)
      return false;

    if (value.getType() == Type.ANY && !allowAny)
      return false;

    if (type == Type.ANY || value.getType() == Type.ANY)
      return true;

    if (isUnion()) {
      for (DataType dt : types) {
        if (dt.isCompatible(value, allowAny))
          return true;
      }
      return false;
    }

    if (isArray())
      return value.isArray() && subtype.isCompatible(value.getSubtype(), allowAny);

    if (isRecord()) {
      if (!value.isRecord())
        return false;
      for (String s : subtypes.keySet()) {
        if (!subtypes.get(s).isCompatible(value.getSubtypes().get(s), allowAny))
          return false;
      }
      return true;
    }
    return type == value.getType();
  }

  public Object toAvro() {
    if (isArray()) {
      Map<String, Object> ret = new HashMap<>();
      ret.put("type", "array");
      ret.put("items", subtype.toAvro());
      return ret;
    }

    if (isRecord()) {
      Map<String, Object> ret = new HashMap<>();
      ret.put("type", "record");
      List<Object> items = new ArrayList<>();

      for (String s : subtypes.keySet()) {
        Map<String, Object> record = new HashMap<>();
        record.put("name", s);
        record.put("type", subtypes.get(s).toAvro());
        items.add(record);
      }
      ret.put("fields", items);
      return ret;
    }

    if (isUnion()) {
      Map<String, Object> ret = new HashMap<>();
      List<Object> items = new ArrayList<>();
      for (DataType dt: types)
        items.add(dt.toAvro());
      ret.put("type", items);
      return ret;
    }

    if (nullable != null && nullable) {
      List<Object> ret = new ArrayList<>();
      ret.add("null");
      ret.add(type.avroType);
      return ret;
    }

    return type.avroType;
  }

  @Override
  public String toString() {
    return "DataType{" + "type=" + type + (subtype != null ? ", subtype=" + subtype : "")
        + (types != null ? ", types=" + types : "") + (subtypes != null ? ", subtypes=" + subtypes : "") +
        (nullable != null ? ", nullable=" + nullable : "") + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DataType dataType = (DataType) o;

    if (type != dataType.type)
      return false;
    if (subtype != null ? !subtype.equals(dataType.subtype) : dataType.subtype != null)
      return false;
    if (types != null ? !types.equals(dataType.types) : dataType.types != null)
      return false;
    return subtypes != null ? subtypes.equals(dataType.subtypes) : dataType.subtypes == null;

  }

  @Override public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
    result = 31 * result + (types != null ? types.hashCode() : 0);
    result = 31 * result + (subtypes != null ? subtypes.hashCode() : 0);
    return result;
  }
}
