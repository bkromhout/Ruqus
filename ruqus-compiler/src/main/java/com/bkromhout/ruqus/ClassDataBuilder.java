package com.bkromhout.ruqus;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.*;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Processes all classes annotated with {@link RealmClass} in order to generate a file with information about all
 * RealmModel subclasses for Ruqus to use at runtime (instead of doing a ton of reflection).
 */
class ClassDataBuilder {

    private final Messager messager;
    private static HashSet<String> realClassNames;
    private static HashSet<String> queryable;
    private static HashMap<String, ClassName> classMap;
    private static HashMap<String, String> visibleNames;
    private static HashMap<String, FieldDataBuilder> fieldData;

    ClassDataBuilder(Messager messager) {
        this.messager = messager;
        realClassNames = new HashSet<>();
        queryable = new HashSet<>();
        classMap = new HashMap<>();
        visibleNames = new HashMap<>();
        fieldData = new HashMap<>();
    }

    boolean hasClasses() {
        return !classMap.isEmpty();
    }

    boolean hasFieldData() {
        return !fieldData.isEmpty();
    }

    Collection<FieldDataBuilder> getFieldDataBuilders() {
        return fieldData.values();
    }

    /**
     * Process all classes which are annotated with {@link RealmClass} and get information needed to generate the class
     * data and various field data files.
     */
    void buildClassAndFieldData(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            if (element.getKind() != ElementKind.CLASS) {
                // Really this shouldn't be an issue since Realm's annotation processor will do real checks, but still.
                error(element, "@RealmClass annotations can only be applied to classes!");
                continue;
            }
            TypeElement typeElement = MoreElements.asType(element);
            if (!isValidRealmClass(typeElement)) continue;

            // Get ClassName object, we'll store this so that we can write out a real type later.
            ClassName className = ClassName.get(typeElement);
            // Get real class name.
            String realName = className.simpleName();
            // Check that the real name of the class isn't already in use.
            if (realClassNames.contains(realName)) {
                error(element, "Failed while processing \"%s\" because there is already a realm object class called " +
                                "\"%s\"; Ruqus currently cannot handle multiple classes with the same name.",
                        className.toString(), realName);
                continue;
            }
            // Check class for queryable and visible name annotations to try and figure out visible name.
            String visibleName = null;
            boolean isQueryable = false;
            if (MoreElements.isAnnotationPresent(typeElement, Queryable.class)) {
                Queryable qAnnot = typeElement.getAnnotation(Queryable.class);
                visibleName = qAnnot.name();
                isQueryable = true;
            }
            if ((visibleName == null || visibleName.isEmpty()) &&
                    MoreElements.isAnnotationPresent(typeElement, VisibleAs.class)) {
                VisibleAs vaAnnot = typeElement.getAnnotation(VisibleAs.class);
                visibleName = vaAnnot.string();
            }
            if (visibleName == null || visibleName.isEmpty()) {
                // Generate a name from TitleCase.
                visibleName = Utils.makeVisName(realName);
            }
            // Check that visible name hasn't already been used.
            if (visibleNames.values().contains(visibleName)) {
                error(element, "Failed while processing \"%s\" because there is already a realm object class which " +
                        "has the visible name \"%s\"; Ruqus currently cannot handle having multiple classes with the " +
                        "same visible name.", className.toString(), visibleName);
                continue;
            }

            // Get field information for all fields in this class.
            FieldDataBuilder fdBuilder = new FieldDataBuilder(realName);
            processFields(typeElement, fdBuilder);

            // Store these locally until we write out the whole class data file.
            realClassNames.add(realName);
            if (isQueryable) queryable.add(realName);
            classMap.put(realName, className);
            visibleNames.put(realName, visibleName);
            fieldData.put(realName, fdBuilder);
        }
    }

    private void processFields(TypeElement typeElement, FieldDataBuilder fdBuilder) {
        for (VariableElement var : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (!SuperficialValidation.validateElement(var)) continue;
            // Ignore if this isn't a field (it could be an enum constant).
            if (var.getKind() != ElementKind.FIELD) continue;
            // Ignore if it has Realm's @Ignore or our @Hide annotation.
            if (MoreElements.isAnnotationPresent(var, Ignore.class) ||
                    MoreElements.isAnnotationPresent(var, Hide.class)) continue;
            // Ignore static fields.
            if (var.getModifiers().contains(Modifier.STATIC)) continue;
            // Ignore if not a valid field.
            TypeMirror varMirror = var.asType();
            if (!isValidFieldType(varMirror)) continue;

            // The field is valid! Start getting data about it; real name first.
            String realFieldName = var.getSimpleName().toString();
            // Check for @VisibleAs.
            String visibleFieldName = null;
            if (MoreElements.isAnnotationPresent(var, VisibleAs.class)) {
                VisibleAs vaAnnot = var.getAnnotation(VisibleAs.class);
                visibleFieldName = vaAnnot.string();
            }
            if (visibleFieldName == null || visibleFieldName.isEmpty()) // Generate a name from camelCase.
                visibleFieldName = Utils.makeVisName(realFieldName);
            // Field type.
            TypeName fieldType = TypeName.get(varMirror);
            // If field is a RealmList, we need to get the parameter's type too.
            ClassName realmListType = null;
            if (isRealmList(fieldType)) {
                List<? extends TypeMirror> parameterTypes = MoreTypes.asDeclared(varMirror).getTypeArguments();
                realmListType = ClassName.bestGuess(parameterTypes.get(0).toString());
            }
            // Add this field's data to the field data builder.
            fdBuilder.addField(realFieldName, visibleFieldName, fieldType, realmListType);
        }
    }

    private boolean isValidRealmClass(TypeElement classElement) {
        // Must be public, non-abstract, and not a *RealmProxy class.
        return classElement.getModifiers().contains(Modifier.PUBLIC) && !classElement.getModifiers().contains(
                Modifier.ABSTRACT) && !ClassName.get(classElement).simpleName().contains("RealmProxy");
    }

    private boolean isValidFieldType(TypeMirror fieldType) {
        switch (fieldType.getKind()) {
            // These primitive types are valid immediately.
            case BOOLEAN:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return true;
            // For declared, we need to do a bit of extra work.
            case DECLARED:
                try {
                    // If it's a boxed primitive, other than byte or char, return true.
                    TypeName typeName = TypeName.get(fieldType).unbox();
                    if (typeName.equals(TypeName.BYTE) || typeName.equals(TypeName.CHAR)) return false;
                    if (typeName.isPrimitive()) return true;
                } catch (UnsupportedOperationException e) {
                    // Do nothing, we just did this to try and check if this was a boxed primitive.
                }

                // Strings and java.util.Dates are okay.
                DeclaredType declaredType = MoreTypes.asDeclared(fieldType);
                if (MoreTypes.isTypeOf(String.class, declaredType) || MoreTypes.isTypeOf(Date.class, declaredType))
                    return true;

                // Any of our classes annotated with @RealmClass are okay.
                if (Utils.isSubtypeOfType(fieldType, TypeNames.REALM_MODEL.toString())) return true;

                // RealmLists are okay as well.
                TypeName fieldTypeName = TypeName.get(fieldType);
                if (isRealmList(fieldTypeName)) return true;

                // Everything else is bad.
                return false;
            // We ignore these types.
            case ARRAY:
            case BYTE:
            case CHAR:
            case ERROR:
            case EXECUTABLE:
            case NONE:
            case NULL:
            case OTHER:
            case PACKAGE:
            case TYPEVAR:
            case UNION:
            case VOID:
            case WILDCARD:
            default:
                return false;
        }
    }

    private boolean isRealmList(TypeName className) {
        return className != null && className.toString().contains(TypeNames.REALM_LIST.simpleName());
    }

    /**
     * Build the JavaFile object which will create the "Ruqus$$RuqusClassData.java" file.
     * @return JavaFile.
     */
    JavaFile brewClassDataFile() {
        // Build static init block.
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        // Loop through real names.
        String addRealNameStmt = "realNames.add($S)";
        staticBlockBuilder.add("// Add all real names of classes.\n");
        for (String realName : realClassNames) staticBlockBuilder.addStatement(addRealNameStmt, realName);

        // Loop through queryable names.
        String addQueryableNameStmt = "queryable.add($S)";
        staticBlockBuilder.add("// Add names of classes annotated with @Queryable.\n");
        for (String queryableName : queryable) staticBlockBuilder.addStatement(addQueryableNameStmt, queryableName);

        // Loop through class object names.
        String addClassStmt = "classMap.put($S, $T.class)";
        staticBlockBuilder.add("// Add class objects.\n");
        for (Map.Entry<String, ClassName> entry : classMap.entrySet())
            staticBlockBuilder.addStatement(addClassStmt, entry.getKey(), entry.getValue());

        // Loop through visible names.
        String addVisibleNameStmt = "visibleNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names.\n");
        for (Map.Entry<String, String> entry : visibleNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNameStmt, entry.getKey(), entry.getValue());

        // Loop through field data classes.
        String addFieldDataStmt = "fieldData.put($S, new $T())";
        staticBlockBuilder.add("// Add field data classes.\n");
        for (Map.Entry<String, FieldDataBuilder> entry : fieldData.entrySet())
            staticBlockBuilder.addStatement(addFieldDataStmt, entry.getKey(), entry.getValue().getClassName());

        // Finally, build this code block.
        CodeBlock staticBlock = staticBlockBuilder.build();

        // Build class.
        TypeSpec clazz = TypeSpec.classBuilder(C.GEN_CLASS_DATA_CLASS_NAME)
                                 .superclass(TypeNames.CLASS_DATA)
                                 .addModifiers(Modifier.FINAL)
                                 .addStaticBlock(staticBlock)
                                 .build();

        // Build file and return it.
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating " + C.GEN_CLASS_DATA_CLASS_NAME);
        return JavaFile.builder(C.GEN_PKG, clazz)
                       .addFileComment(C.GEN_CODE_FILE_COMMENT)
                       .build();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
