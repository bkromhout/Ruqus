package com.bkromhout.ruqus;

import java.util.Date;

/**
 * Helps us cast our condition arguments to and from Strings for easy serialization.
 */
public class Caster {
    private static final String SEP = "::";
    private static final String BOOLEAN = "BOOLEAN";
    private static final String DATE = "DATE";
    private static final String DOUBLE = "DOUBLE";
    private static final String FLOAT = "FLOAT";
    private static final String INTEGER = "INTEGER";
    private static final String LONG = "LONG";
    private static final String SHORT = "SHORT";
    private static final String STRING = "STRING";

    public static String makeString(Object data) {
        Class clazz = data.getClass();
        if (Boolean.class == clazz || boolean.class == clazz) return String.valueOf(data) + SEP + BOOLEAN;
        else if (Date.class == clazz) return String.valueOf(((Date) data).getTime()) + SEP + DATE;
        else if (Double.class == clazz || double.class == clazz) return String.valueOf(data) + SEP + DOUBLE;
        else if (Float.class == clazz || float.class == clazz) return String.valueOf(data) + SEP + FLOAT;
        else if (Integer.class == clazz || int.class == clazz) return String.valueOf(data) + SEP + INTEGER;
        else if (Long.class == clazz || long.class == clazz) return String.valueOf(data) + SEP + LONG;
        else if (Short.class == clazz || short.class == clazz) return String.valueOf(data) + SEP + SHORT;
        else if (String.class == clazz) return data + SEP + STRING;
        else throw new IllegalArgumentException("Invalid data type.");
    }

    public static Object parseString(String data) {
        String[] parts = data.split("\\Q" + SEP + "\\E");
        switch (parts[1]) {
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
}
