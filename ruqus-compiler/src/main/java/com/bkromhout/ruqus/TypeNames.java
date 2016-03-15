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
    static final ClassName REALM_OBJ = ClassName.bestGuess(C.REALM_OBJ);
    static final ClassName REALM_LIST = ClassName.bestGuess(C.REALM_LIST);

    // Ruqus Types.
    static final ClassName CLASS_DATA_CLASS = ClassName.get(C.GEN_PKG, C.CLASS_DATA);
    static final ClassName FIELD_DATA_CLASS = ClassName.get(C.GEN_PKG, C.FIELD_DATA);
    static final ClassName RUQ_TRANS_CLASS = ClassName.get(C.GEN_PKG, C.RUQ_TRANSFORMER);

    // First-level Parameterized types
    static final TypeName ANY_CLASS = ParameterizedTypeName.get(CLASS, WildcardTypeName.subtypeOf(TypeName.OBJECT));
    static final TypeName ANY_REALM_OBJ_CLASS = ParameterizedTypeName.get(CLASS, WildcardTypeName.subtypeOf(REALM_OBJ));
    static final TypeName ANY_RUQ_TRANS_CLASS = ParameterizedTypeName.get(CLASS, RUQ_TRANS_CLASS);

    // Second-level Parameterized types.
    static final TypeName S_ARRAY_LIST = ParameterizedTypeName.get(ClassName.get(ArrayList.class), STRING);
    static final TypeName S_HASH_SET = ParameterizedTypeName.get(ClassName.get(HashSet.class), STRING);
    static final TypeName S_STRING_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, STRING);
    static final TypeName S_ANY_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, ANY_CLASS);
    static final TypeName S_ANY_REALM_OBJ_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING,
            ANY_REALM_OBJ_CLASS);
    static final TypeName S_ANY_RUQ_TRANS_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING,
            ANY_RUQ_TRANS_CLASS);
}
