package com.bkromhout.ruqus;

import io.realm.RealmList;
import io.realm.RealmObject;

import java.util.Date;

/**
 * Represents field data types so that we don't have to compare Class objects as much.
 */
public enum FieldType {
    BOOLEAN("BOOLEAN", Boolean.class),
    DATE("DATE", Date.class),
    DOUBLE("DOUBLE", Double.class),
    FLOAT("FLOAT", Float.class),
    INTEGER("INTEGER", Integer.class),
    LONG("LONG", Long.class),
    SHORT("SHORT", Short.class),
    STRING("STRING", String.class),
    REALM_OBJECT("REALM_OBJECT", RealmObject.class),
    REALM_LIST("REALM_LIST", RealmList.class);

    private final String name;
    private final Class clazz;

    FieldType(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getTypeName() {
        return name;
    }

    public Class getClazz() {
        return clazz;
    }

    public FieldType fromName(String name) {
        switch (name) {
            case "BOOLEAN":
                return BOOLEAN;
            case "DATE":
                return DATE;
            case "DOUBLE":
                return DOUBLE;
            case "FLOAT":
                return FLOAT;
            case "INTEGER":
                return INTEGER;
            case "LONG":
                return LONG;
            case "SHORT":
                return SHORT;
            case "STRING":
                return STRING;
            case "REALM_OBJECT":
                return REALM_OBJECT;
            case "REALM_LIST":
                return REALM_LIST;
            default:
                throw new IllegalArgumentException("Invalid name.");
        }
    }

    public FieldType fromClazz(Class clazz) {
        if (Boolean.class == clazz || boolean.class == clazz) return BOOLEAN;
        else if (Date.class == clazz) return DATE;
        else if (Double.class == clazz || double.class == clazz) return DOUBLE;
        else if (Float.class == clazz || float.class == clazz) return FLOAT;
        else if (Integer.class == clazz || int.class == clazz) return INTEGER;
        else if (Long.class == clazz || long.class == clazz) return LONG;
        else if (Short.class == clazz || short.class == clazz) return SHORT;
        else if (String.class == clazz) return STRING;
        else if (RealmObject.class.isAssignableFrom(clazz)) return REALM_OBJECT;
        else if (RealmList.class.isAssignableFrom(clazz)) return REALM_LIST;
        else throw new IllegalArgumentException("Invalid class.");
    }

    public boolean isNumber() {
        switch (this) {
            case DOUBLE:
            case FLOAT:
            case INTEGER:
            case LONG:
            case SHORT:
                return true;
            default:
                return false;
        }
    }
}
