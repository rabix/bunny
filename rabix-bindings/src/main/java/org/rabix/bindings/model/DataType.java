package org.rabix.bindings.model;

import java.util.Map;
import java.util.Set;


public class DataType {
    public enum Type {
        PRIMITIVE, UNION, ARRAY, RECORD, FILE, DIRECTORY, ANY, BOOLEAN;
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
        for (DataType dt: types) {
            if (dt.getType() == t)
                return true;
        }
        return false;
    }

}
