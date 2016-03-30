package com.bkromhout.ruqus;

import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Utility methods for the processor.
 */
class Utils {
    /**
     * Turns both "camelCase" and "TitleCase" strings into "Camel Case" and "Title Case" strings.
     * <p/>
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

    /**
     * Check if {@code typeMirror} is [a subtype of] {@code otherType}.
     * @param typeMirror Mirror for type to check.
     * @param otherType  Fully qualified name to check against.
     * @return True if {@code typeMirror} is [a subtype of] {@code otherType}.
     */
    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (otherType.equals(typeMirror.toString())) return true;
        if (typeMirror.getKind() != TypeKind.DECLARED) return false;

        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) typeString.append(',');
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) return true;
        }

        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) return false;

        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) return true;
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) return true;
        }

        return false;
    }
}
