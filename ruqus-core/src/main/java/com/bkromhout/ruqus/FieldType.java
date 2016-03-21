package com.bkromhout.ruqus;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.Sort;

import java.util.Date;

/**
 * Represents field data types so that we don't have to compare Class objects as much.
 * <p>
 * Also has many convenience methods related to field types.
 */
public enum FieldType {
    BOOLEAN(S.BOOLEAN_NAME, Boolean.class, S.BOOLEAN_PRETTY_SORT),
    DATE(S.DATE_NAME, Date.class, S.DATE_PRETTY_SORT),
    DOUBLE(S.DOUBLE_NAME, Double.class, S.NUMBER_PRETTY_SORT),
    FLOAT(S.FLOAT_NAME, Float.class, S.NUMBER_PRETTY_SORT),
    INTEGER(S.INTEGER_NAME, Integer.class, S.NUMBER_PRETTY_SORT),
    LONG(S.LONG_NAME, Long.class, S.NUMBER_PRETTY_SORT),
    SHORT(S.SHORT_NAME, Short.class, S.NUMBER_PRETTY_SORT),
    STRING(S.STRING_NAME, String.class, S.STRING_PRETTY_SORT),
    REALM_OBJECT(S.REALM_OBJECT_NAME, RealmObject.class, null),
    REALM_LIST(S.REALM_LIST_NAME, RealmList.class, null);

    private final String name;
    private final Class clazz;
    private final String[] prettySortStrings;

    FieldType(String name, Class clazz, String[] prettySortStrings) {
        this.name = name;
        this.clazz = clazz;
        this.prettySortStrings = prettySortStrings;
    }

    public String getTypeName() {
        return name;
    }

    Class getClazz() {
        return clazz;
    }

    String getPrettySortString(Sort sortDir) {
        if (prettySortStrings == null) return null;
        return sortDir == Sort.ASCENDING ? prettySortStrings[0] : prettySortStrings[1];
    }

    String[] getPrettySortStrings() {
        return prettySortStrings;
    }

    static boolean isNumber(FieldType fieldType) {
        switch (fieldType) {
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

    static FieldType fromName(String name) {
        switch (name) {
            case S.BOOLEAN_NAME:
                return BOOLEAN;
            case S.DATE_NAME:
                return DATE;
            case S.DOUBLE_NAME:
                return DOUBLE;
            case S.FLOAT_NAME:
                return FLOAT;
            case S.INTEGER_NAME:
                return INTEGER;
            case S.LONG_NAME:
                return LONG;
            case S.SHORT_NAME:
                return SHORT;
            case S.STRING_NAME:
                return STRING;
            case S.REALM_OBJECT_NAME:
                return REALM_OBJECT;
            case S.REALM_LIST_NAME:
                return REALM_LIST;
            default:
                throw new IllegalArgumentException("Invalid name.");
        }
    }

    static FieldType fromClazz(Class clazz) {
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

    static String makeDataString(Object data) {
        FieldType type = fromClazz(data.getClass());
        switch (type) {
            case DATE:
                return String.valueOf(((Date) data).getTime()) + S.SEP + type.getTypeName();
            case STRING:
                return data + S.SEP + type.getTypeName();
            case REALM_OBJECT:
            case REALM_LIST:
                throw new IllegalArgumentException("Invalid data type.");
            default:
                return String.valueOf(data) + S.SEP + type.getTypeName();
        }
    }

    static Object parseDataString(String data) {
        String[] parts = data.split("\\Q" + S.SEP + "\\E");
        switch (fromName(parts[1])) {
            case BOOLEAN:
                return Boolean.valueOf(parts[0]);
            case DATE:
                return new Date(Long.valueOf(parts[0]));
            case DOUBLE:
                return Double.valueOf(parts[0]);
            case FLOAT:
                return Float.valueOf(parts[0]);
            case INTEGER:
                return Integer.valueOf(parts[0]);
            case LONG:
                return Long.valueOf(parts[0]);
            case SHORT:
                return Short.valueOf(parts[0]);
            case STRING:
                return parts[0];
            default:
                throw new IllegalArgumentException(String.format("Invalid data string \"%s\".", data));
        }
    }

    private static class S {
        static final String SEP = "::";
        static final String BOOLEAN_NAME = "BOOLEAN";
        static final String DATE_NAME = "DATE";
        static final String DOUBLE_NAME = "DOUBLE";
        static final String FLOAT_NAME = "FLOAT";
        static final String INTEGER_NAME = "INTEGER";
        static final String LONG_NAME = "LONG";
        static final String SHORT_NAME = "SHORT";
        static final String STRING_NAME = "STRING";
        static final String REALM_OBJECT_NAME = "REALM_OBJECT";
        static final String REALM_LIST_NAME = "REALM_LIST";

        static final String[] BOOLEAN_PRETTY_SORT = new String[] {"False before True", "True before False"};
        static final String[] DATE_PRETTY_SORT = new String[] {"Earliest to Latest", "Latest to Earliest"};
        static final String[] NUMBER_PRETTY_SORT = new String[] {"Lowest to Highest", "Highest to Lowest"};
        static final String[] STRING_PRETTY_SORT = new String[] {"a to Z", "Z to a"};
    }
}
