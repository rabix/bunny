package org.rabix.bindings.model;

import java.util.Map;
import java.util.Set;

public class DataType {
  public enum Type {
    UNION,
    ARRAY,
    RECORD,
    FILE,
    DIRECTORY,
    ANY,
    NULL,
    BOOLEAN(Boolean.class),
    STRING(String.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class);

    public final Class<?> primitiveType;

    Type() {
      primitiveType = null;
    }

    Type(Class<?> primitiveType) {
      this.primitiveType = primitiveType;
    }
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

  private final Type type;
  private DataType subtype;
  private Set<DataType> types;
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

  public boolean isRecord() {
    return type == Type.RECORD;
  }

  public boolean isArray() {
    return type == Type.ARRAY;
  }

  public boolean isUnion() {
    return type == Type.UNION;
  }

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
}
