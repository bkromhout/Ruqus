package com.bkromhout.ruqus;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A collection of TypeNames and ClassNames which are used in many places. Some are private since they are only needed
 * to instantiate others.
 */
final class TypeNames {
    // Java Types.
    private static final ClassName CLASS = ClassName.get(Class.class);
    static final ClassName STRING = ClassName.get(String.class);
    private static final ClassName HASH_MAP = ClassName.get(HashMap.class);

    // Realm Types.
    static final ClassName REALM_MODEL = ClassName.bestGuess(C.REALM_MODEL);
    static final ClassName REALM_LIST = ClassName.bestGuess(C.REALM_LIST);

    // Ruqus Types.
    static final ClassName CLASS_DATA = ClassName.get(C.GEN_PKG, C.CLASS_DATA);
    static final ClassName FIELD_DATA = ClassName.get(C.GEN_PKG, C.FIELD_DATA);
    static final ClassName TRANS_DATA = ClassName.get(C.GEN_PKG, C.TRANSFORMER_DATA);
    static final ClassName RUQ_TRANS = ClassName.get(C.GEN_PKG, C.RUQ_TRANSFORMER);

    // First-level Parameterized types
    static final TypeName ANY = ParameterizedTypeName.get(CLASS, WildcardTypeName.subtypeOf(TypeName.OBJECT));
    static final TypeName ANY_REALM_MODEL = ParameterizedTypeName.get(CLASS, WildcardTypeName.subtypeOf(REALM_MODEL));

    // Second-level Parameterized types.
    static final TypeName S_ARRAY_LIST = ParameterizedTypeName.get(ClassName.get(ArrayList.class), STRING);
    static final TypeName S_HASH_SET = ParameterizedTypeName.get(ClassName.get(HashSet.class), STRING);
    static final TypeName S_STRING_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, STRING);
    static final TypeName S_ANY_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, ANY);
    static final TypeName S_ANY_REALM_OBJ_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, ANY_REALM_MODEL);
}
