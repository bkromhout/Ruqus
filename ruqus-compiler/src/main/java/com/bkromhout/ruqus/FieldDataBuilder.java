package com.bkromhout.ruqus;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Helps generate *$$RuqusFieldData classes.
 */
public class FieldDataBuilder {
    private static final String REAL_FIELD_NAMES = "realFieldNames";
    private static final String VISIBLE_FIELD_NAMES = "visibleFieldNames";
    private static final String TYPES = "types";
    private static final String REALM_LIST_TYPES = "realmListTypes";

    private static final String GET_FIELD_NAMES = "getFieldNames";
    private static final String GET_VISIBLE_NAMES = "getVisibleNames";
    private static final String VISIBLE_NAME_OF = "visibleNameOf";
    private static final String FIELD_TYPE = "fieldType";
    private static final String REALM_LIST_TYPE = "realmListType";
    private static final String IS_REALM_OBJECT_TYPE = "isRealmObjectType";
    private static final String IS_REALM_LIST_TYPE = "isRealmListType";

    ClassName className;
    HashSet<String> realNames;
    HashMap<String, String> visibleNames;
    HashMap<String, TypeName> types;
    HashMap<String, ClassName> realmListTypes;

    FieldDataBuilder(String className) {
        this.className = ClassName.get(C.GEN_PKG, className + C.FIELD_DATA_SUFFIX);
        realNames = new HashSet<>();
        visibleNames = new HashMap<>();
        types = new HashMap<>();
        realmListTypes = new HashMap<>();
    }

    void addField(String realName, String visibleName, TypeName type, ClassName realmListType) {
        if (realNames.add(realName)) {
            visibleNames.put(realName, visibleName);
            types.put(realName, type);
            if (realmListType != null) realmListTypes.put(realName, realmListType);
        }
    }

    JavaFile brewJava() {
        // Build class.
        TypeSpec clazz = TypeSpec.classBuilder(className.simpleName())
                                 .superclass(TypeNames.FIELD_DATA_CLASS)
                                 .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                 .addField(buildRealNamesField())
                                 .addField(buildVisibleNamesField())
                                 .addField(buildTypesField())
                                 .addField(buildRealmListTypesField())
                                 .addStaticBlock(buildStaticInitBlock())
                                 .addMethod(buildGetFieldNames())
                                 .addMethod(buildGetVisibleNames())
                                 .addMethod(buildVisibleNameOf())
                                 .addMethod(buildFieldType())
                                 .addMethod(buildRealmListType())
                                 .addMethod(buildIsRealmObjectType())
                                 .addMethod(buildIsRealmListType())
                                 .build();

        // Build and return file.
        return JavaFile.builder(className.packageName(), clazz)
                       .addFileComment(C.GEN_CODE_FILE_COMMENT)
                       .build();
    }

    /*
     * Field Builders.
     */

    private FieldSpec buildRealNamesField() {
        return FieldSpec.builder(TypeNames.S_HASH_SET, REAL_FIELD_NAMES, Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL)
                        .initializer("new $T()", TypeNames.S_HASH_SET)
                        .build();
    }

    private FieldSpec buildVisibleNamesField() {
        return FieldSpec.builder(TypeNames.S_STRING_HASH_MAP, VISIBLE_FIELD_NAMES, Modifier.PRIVATE,
                Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", TypeNames.S_STRING_HASH_MAP)
                        .build();
    }

    private FieldSpec buildTypesField() {
        return FieldSpec.builder(TypeNames.S_ANY_CLASS_HASH_MAP, TYPES, Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL)
                        .initializer("new $T()", TypeNames.S_ANY_CLASS_HASH_MAP)
                        .build();
    }

    private FieldSpec buildRealmListTypesField() {
        return FieldSpec.builder(TypeNames.S_ANY_REALM_OBJ_CLASS_HASH_MAP, REALM_LIST_TYPES, Modifier.PRIVATE,
                Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", TypeNames.S_ANY_REALM_OBJ_CLASS_HASH_MAP)
                        .build();
    }

    /*
     * Static init block builder.
     */

    private CodeBlock buildStaticInitBlock() {
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        // Loop through real names.
        String addRealNameStmt = "$L.add($S)";
        staticBlockBuilder.add("// Add real field names.\n");
        for (String string : realNames) staticBlockBuilder.addStatement(addRealNameStmt, REAL_FIELD_NAMES, string);

        // Loop through visible names.
        String addVisibleNameStmt = "$L.put($S, $S)";
        staticBlockBuilder.add("// Add visible field names.\n");
        for (Map.Entry<String, String> entry : visibleNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNameStmt, VISIBLE_FIELD_NAMES, entry.getKey(), entry.getValue());

        // Loop through types.
        String addTypeStmt = "$L.put($S, $T.class)";
        staticBlockBuilder.add("// Add field types.\n");
        for (Map.Entry<String, TypeName> entry : types.entrySet())
            staticBlockBuilder.addStatement(addTypeStmt, TYPES, entry.getKey(),
                    entry.getValue().toString().contains("RealmList") ? TypeNames.REALM_LIST : entry.getValue());

        // Loop through realm list types.
        String addRealmListTypeStmt = "$L.put($S, $T.class)";
        staticBlockBuilder.add("// For fields of RealmList type, add the RealmList types.\n");
        for (Map.Entry<String, ClassName> entry : realmListTypes.entrySet())
            staticBlockBuilder.addStatement(addRealmListTypeStmt, REALM_LIST_TYPES, entry.getKey(), entry.getValue());

        return staticBlockBuilder.build();
    }

    /*
     * Method Builders.
     */

    private MethodSpec buildGetFieldNames() {
        return MethodSpec.methodBuilder(GET_FIELD_NAMES)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeNames.S_ARRAY_LIST)
                         .addStatement("return new $T($L)", TypeNames.S_ARRAY_LIST, REAL_FIELD_NAMES)
                         .build();
    }

    private MethodSpec buildGetVisibleNames() {
        return MethodSpec.methodBuilder(GET_VISIBLE_NAMES)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeNames.S_ARRAY_LIST)
                         .addStatement("return new $T($L.values())", TypeNames.S_ARRAY_LIST, VISIBLE_FIELD_NAMES)
                         .build();
    }

    private MethodSpec buildVisibleNameOf() {
        String paramName = "realFieldName";
        return MethodSpec.methodBuilder(VISIBLE_NAME_OF)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeNames.STRING)
                         .addParameter(TypeNames.STRING, paramName)
                         .addStatement("return $L.get($L)", VISIBLE_FIELD_NAMES, paramName)
                         .build();
    }

    private MethodSpec buildFieldType() {
        String paramName = "realFieldName";
        return MethodSpec.methodBuilder(FIELD_TYPE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeNames.ANY_CLASS)
                         .addParameter(TypeNames.STRING, paramName)
                         .addStatement("return $L.get($L)", TYPES, paramName)
                         .build();
    }

    private MethodSpec buildRealmListType() {
        String paramName = "realFieldName";
        return MethodSpec.methodBuilder(REALM_LIST_TYPE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeNames.ANY_REALM_OBJ_CLASS)
                         .addParameter(TypeNames.STRING, paramName)
                         .addStatement("return $L.get($L)", REALM_LIST_TYPES, paramName)
                         .build();
    }

    private MethodSpec buildIsRealmObjectType() {
        String paramName = "realFieldName";
        return MethodSpec.methodBuilder(IS_REALM_OBJECT_TYPE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeName.BOOLEAN)
                         .addParameter(TypeNames.STRING, paramName)
                         .addStatement("return $L($L).isInstance($T.class)", FIELD_TYPE, paramName, TypeNames.REALM_OBJ)
                         .build();
    }

    private MethodSpec buildIsRealmListType() {
        String paramName = "realFieldName";
        return MethodSpec.methodBuilder(IS_REALM_LIST_TYPE)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                         .returns(TypeName.BOOLEAN)
                         .addParameter(TypeNames.STRING, paramName)
                         .addStatement("return $L.containsKey($L)", REALM_LIST_TYPES, paramName)
                         .build();
    }

    ClassName getClassName() {
        return className;
    }
}
