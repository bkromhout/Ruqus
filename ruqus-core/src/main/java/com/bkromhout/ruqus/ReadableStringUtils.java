package com.bkromhout.ruqus;

import com.squareup.phrase.ListPhrase;

import java.util.Arrays;

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
    public static String argsToString(String transformerName, Object[] args) {
        int numArgs = Ruqus.getTransformerData().numArgsOf(transformerName);
        if (numArgs == 0)
            return "";
        else if (numArgs == 1)
            return " " + String.valueOf(args[0]);
        else if (numArgs == C.VAR_ARGS || numArgs > 1)
            return " " + ListPhrase.from(" and ", ", ", ", and ")
                                   .join(Arrays.asList(args))
                                   .toString();
        else throw new IllegalArgumentException("numArgs < -1.");
    }
}
