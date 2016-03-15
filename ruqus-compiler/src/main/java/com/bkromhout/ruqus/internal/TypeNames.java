package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.C;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Many of these are used all over the place.
 */
final class TypeNames {
    private static final ClassName ARRAY_LIST = ClassName.get(ArrayList.class);
    private static final ClassName HASH_SET = ClassName.get(HashSet.class);
    private static final ClassName HASH_MAP = ClassName.get(HashMap.class);
    static final ClassName STRING = ClassName.get(String.class);
    static final ClassName REALM_OBJ = ClassName.bestGuess("io.realm.RealmObject");
    static final ClassName REALM_LIST = ClassName.bestGuess("io.realm.RealmList");
    private static final ClassName CLASS = ClassName.get(Class.class);
    private static final WildcardTypeName ANY = WildcardTypeName.subtypeOf(TypeName.OBJECT);
    private static final WildcardTypeName ANY_REALM_OBJ = WildcardTypeName.subtypeOf(REALM_OBJ);
    static final TypeName ANY_CLASS = ParameterizedTypeName.get(CLASS, ANY);
    private static final TypeName ANY_REALM_OBJ_CLASS = ParameterizedTypeName.get(CLASS, ANY_REALM_OBJ);
    static final TypeName STRING_ARRAY_LIST = ParameterizedTypeName.get(ARRAY_LIST, STRING);
    static final TypeName STRING_HASH_SET = ParameterizedTypeName.get(HASH_SET, STRING);
    static final TypeName STRING_STRING_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, STRING);
    static final TypeName STRING_ANY_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING, ANY_CLASS);
    static final TypeName STRING_ANY_REALM_OBJ_CLASS_HASH_MAP = ParameterizedTypeName.get(HASH_MAP, STRING,
            ANY_REALM_OBJ_CLASS);
    static final ClassName CLASS_DATA_CLASS = ClassName.get(C.GEN_PKG, C.CLASS_DATA);
    static final ClassName FIELD_DATA_CLASS = ClassName.get(C.GEN_PKG, C.FIELD_DATA);
}
