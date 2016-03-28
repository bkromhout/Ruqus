package com.bkromhout.ruqus;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for the processor.
 */
class Utils {
    /**
     * Turns both "camelCase" and "TitleCase" strings into "Camel Case" and "Title Case" strings.
     * <p>
     * Credit for the majority of this method goes to user "polygenelubricants" on <a
     * href="http://stackoverflow.com/a/2560017/2263250">this stack overflow post</a>.
     * @param string String to convert.
     * @return Converted string.
     */
    static String makeVisName(String string) {
        // Make sure the first letter is capitalized.
        string = StringUtils.capitalize(string);
        // Now do the magic.
        return string.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])", " ");
    }
}
