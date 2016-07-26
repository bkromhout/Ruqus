package com.bkromhout.ruqus;

import com.squareup.phrase.ListPhrase;

import java.util.ArrayList;
import java.util.Date;

/**
 * Utility methods to help with creating human-readable strings. Public so that developers may use them when
 * implementing their own {@link RUQTransformer}s. Any strings returned by methods in this class should be considered
 * human-readable.
 */
@SuppressWarnings("WeakerAccess")
public class ReadableStringUtils {
    /**
     * Takes a fully-qualified transformer name and an array of arguments and returns a human-readable list of the
     * arguments. Will have a single leading space, unless the returned string is the empty string.
     * @param transformerName Fully-qualified name of the transformer that the arguments are intended for.
     * @param args            Arguments which will be used with the transformer.
     * @return List of arguments in human-readable form. Might be the empty string.
     */
    public static String argsToString(FieldType fieldType, String transformerName, Object[] args) {
        int numArgs = Ruqus.getTransformerData().numArgsOf(transformerName);
        if (numArgs == 0)
            return "";
        else if (numArgs == 1)
            return " " + argToString(fieldType, args[0]);
        else if (numArgs == C.VAR_ARGS || numArgs > 1) {
            ArrayList<String> stringArgs = new ArrayList<>(args.length);
            for (Object arg : args) stringArgs.add(argToString(fieldType, arg));
            return " " + ListPhrase.from(" and ", ", ", ", and ")
                                   .join(stringArgs)
                                   .toString();
        } else throw new IllegalArgumentException("numArgs < -1.");
    }

    /**
     * Converts an argument to a string based on a {@link FieldType}.
     * @param fieldType Field type.
     * @param arg       Argument.
     * @return Argument as a string.
     * @throws IllegalArgumentException if {@code fieldType} is {@link FieldType#REALM_MODEL} or {@link
     *                                  FieldType#REALM_LIST}.
     */
    public static String argToString(FieldType fieldType, Object arg) {
        if (FieldType.isNumber(fieldType) || fieldType == FieldType.BOOLEAN) return String.valueOf(arg);
        else if (fieldType == FieldType.STRING) return (String) arg;
        else if (fieldType == FieldType.DATE) return Util.stringFromDate((Date) arg);
        else throw new IllegalArgumentException("Field type must not be REALM_MODEL or REALM_LIST.");
    }
}
