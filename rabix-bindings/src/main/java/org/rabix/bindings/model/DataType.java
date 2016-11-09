package org.rabix.bindings.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataType {
  public enum Type {
    UNION, ARRAY, RECORD, FILE, DIRECTORY, ANY, NULL,
    BOOLEAN(Boolean.class), STRING(String.class), INT(Integer.class),
    LONG(Long.class), FLOAT(Float.class), DOUBLE(Double.class);

    public final Class<?> primitiveType;

    Type() {
      primitiveType = null;
    }

    Type(Class<?> primitiveType) {
      this.primitiveType = primitiveType;
    }
  }

  @JsonCreator
  public DataType(@JsonProperty("type") String type) {
    this.type = Type.valueOf(type);
  }

  public DataType(Type type) {
    this.type = type;
  }

  // Constructor for UNION
  public DataType(Type type, Set<DataType> types) {
    this.type = type;
    this.types = types;
  }

  // Constructor for ARRAY
  public DataType(Type type, DataType subtype) {
    this.type = type;
    this.subtype = subtype;
  }

  // Constructor for RECORD
  public DataType(Type type, Map<String, DataType> subtypes) {
    this.type = type;
    this.subtypes = subtypes;
  }

  @JsonProperty("type")
  private final Type type;

  @JsonProperty("subtype")
  private DataType subtype;

  @JsonProperty("types")
  private Set<DataType> types;

  @JsonProperty("subtypes")
  private Map<String, DataType> subtypes;

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
    if (value == null)
      return false;

    if (type == Type.ANY || value.getType() == Type.ANY)
      return true;

    if (isUnion()) {
      for (DataType dt : types) {
        if (dt.isCompatible(value))
          return true;
      }
      return false;
    }

    if (isArray())
      return value.isArray() && subtype.isCompatible(value.getSubtype());

    if (isRecord()) {
      if (!value.isRecord())
        return false;
      for (String s : subtypes.keySet()) {
        if (!subtypes.get(s).isCompatible(value.getSubtypes().get(s)))
          return false;
      }
      return true;
    }
    return type == value.getType();
  }

  @Override
  public String toString() {
    return "DataType{" + "type=" + type + (subtype != null ? ", subtype=" + subtype : "")
        + (types != null ? ", types=" + types : "") + (subtypes != null ? ", subtypes=" + subtypes : "") + '}';
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
