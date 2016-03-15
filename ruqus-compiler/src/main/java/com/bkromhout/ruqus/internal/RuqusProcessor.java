package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.C;
import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {

    public static Processor instance;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    // Help generate the Ruqus$$RuqusClassData file.
    private static HashSet<String> realClassNames;
    private static HashSet<String> queryable;
    private static HashMap<String, ClassName> classMap;
    private static HashMap<String, String> visibleNames;
    private static HashMap<String, FieldDataBuilder> fieldData;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            // Our annotations.
            add(Queryable.class.getCanonicalName());
            add(Transformer.class.getCanonicalName());
            add(Hide.class.getCanonicalName());
            add(VisibleAs.class.getCanonicalName());
            // Realm annotations.
            add(RealmClass.class.getCanonicalName());
            add(Ignore.class.getCanonicalName());
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        instance = this;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();

        realClassNames = new HashSet<>();
        queryable = new HashSet<>();
        classMap = new HashMap<>();
        visibleNames = new HashMap<>();
        fieldData = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            if (element.getKind() != ElementKind.CLASS) {
                // Really this shouldn't be an issue since Realm's annotation processor will do real checks, but still.
                error(element, "RealmClass annotations can only be applied to classes!");
                continue;
            }
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            if (!isValidRealmClass(typeElement)) continue;

            // Get ClassName object, we'll store this so that we can write out a real type later.
            ClassName className = ClassName.get(typeElement);
            // Get real class name.
            String realName = className.simpleName();
            // Check class for queryable and visible name annotations to try and figure out visible name.
            String visibleName;
            boolean isQueryable = false;
            if (MoreElements.isAnnotationPresent(typeElement, Queryable.class)) {
                Queryable qAnnot = typeElement.getAnnotation(Queryable.class);
                visibleName = qAnnot.name();
                isQueryable = true;
            } else if (MoreElements.isAnnotationPresent(typeElement, VisibleAs.class)) {
                VisibleAs vaAnnot = typeElement.getAnnotation(VisibleAs.class);
                visibleName = vaAnnot.string();
            } else {
                visibleName = realName;
            }

            // Get field information for all fields in this class.
            FieldDataBuilder fdBuilder = new FieldDataBuilder(realName);
            for (VariableElement var : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                if (!SuperficialValidation.validateElement(var)) continue;
                // Ignore if this isn't a field (it could be an enum constant).
                if (var.getKind() != ElementKind.FIELD) continue;
                // Ignore if it has Realm's @Ignore or our @Hide annotation.
                if (MoreElements.isAnnotationPresent(var, Ignore.class) ||
                        MoreElements.isAnnotationPresent(var, Hide.class)) continue;
                // Ignore if not a valid field.
                TypeMirror varMirror = var.asType();
                if (!isValidFieldType(varMirror)) continue;

                // The field is valid! Start getting data about it; real name first.
                String realFieldName = var.getSimpleName().toString();
                // Check for @VisibleAs.
                String visibleFieldName = realFieldName;
                if (MoreElements.isAnnotationPresent(var, VisibleAs.class)) {
                    VisibleAs vaAnnot = var.getAnnotation(VisibleAs.class);
                    visibleFieldName = vaAnnot.string();
                }
                // Field type.
                ClassName fieldType = ClassName.bestGuess(varMirror.toString());
                // If field is a RealmList, we need to get the parameter's type too.
                ClassName realmListType = null;
                if (isRealmList(fieldType)) {
                    List<? extends TypeMirror> parameterTypes = MoreTypes.asDeclared(varMirror).getTypeArguments();
                    realmListType = ClassName.bestGuess(parameterTypes.get(0).toString());
                }

                // Add this field's data to the field data builder.
                fdBuilder.addField(realFieldName, visibleFieldName, fieldType, realmListType);
            }

            // Store these locally until we write out the whole class data file.
            realClassNames.add(realName);
            if (isQueryable) queryable.add(realName);
            classMap.put(realName, className);
            visibleNames.put(realName, visibleName);
            fieldData.put(realName, fdBuilder);
        }

        // TODO Process Transformers and build transformer data file.

        // Write out all files.
        try {
            // Write out field data files.
            for (FieldDataBuilder fdb : fieldData.values()) fdb.brewJava().writeTo(filer);
            // Write out class data file.
            brewClassDataFile().writeTo(filer);
            // TODO Write out transformers data file.

        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return true;
    }

    private JavaFile brewClassDataFile() {
        // Build static init block.
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        // Loop through real names.
        String addRealNameStmt = "realNames.add($S)";
        staticBlockBuilder.add("// Add all real names of classes.");
        for (String realName : realClassNames) staticBlockBuilder.addStatement(addRealNameStmt, realName);

        // Loop through queryable names.
        String addQueryableNameStmt = "queryable.add($S)";
        staticBlockBuilder.add("// Add names of classes annotated with @Queryable.");
        for (String queryableName : queryable) staticBlockBuilder.addStatement(addQueryableNameStmt, queryableName);

        // Loop through class object names.
        String addClassStmt = "classMap.put($S, $T.class)";
        staticBlockBuilder.add("// Add class objects.");
        for (Map.Entry<String, ClassName> entry : classMap.entrySet())
            staticBlockBuilder.addStatement(addClassStmt, entry.getKey(), entry.getValue());

        // Loop through visible names.
        String addVisibleNameStmt = "visibleNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names.");
        for (Map.Entry<String, String> entry : visibleNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNameStmt, entry.getKey(), entry.getValue());

        // Loop through field data classes.
        String addFieldDataStmt = "fieldDatas.put($S, new $T())";
        staticBlockBuilder.add("// Add field data classes.");
        for (Map.Entry<String, FieldDataBuilder> entry : fieldData.entrySet())
            staticBlockBuilder.addStatement(addFieldDataStmt, entry.getKey(), entry.getValue().getClassName());

        // Finally, build this code block.
        CodeBlock staticBlock = staticBlockBuilder.build();

        // Build class.
        TypeSpec clazz = TypeSpec.classBuilder(C.GEN_CLASS_DATA_CLASS_NAME)
                                 .superclass(TypeNames.CLASS_DATA_CLASS)
                                 .addModifiers(Modifier.FINAL)
                                 .addStaticBlock(staticBlock)
                                 .build();

        // Build file and return it.
        return JavaFile.builder(C.GEN_PKG, clazz)
                       .addFileComment(C.GEN_CODE_FILE_COMMENT)
                       .build();
    }

    private boolean isValidRealmClass(TypeElement classElement) {
        // Must be public and non-abstract.
        return classElement.getModifiers().contains(Modifier.PUBLIC) && !classElement.getModifiers().contains(
                Modifier.ABSTRACT);
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
                ClassName fieldClassName = ClassName.bestGuess(fieldType.toString());
                if (isARealmObjClass(fieldClassName)) return true;

                // RealmLists are okay as well.
                if (isRealmList(fieldClassName)) return true;

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

    static boolean isARealmObjClass(ClassName className) {
        return classMap.values().contains(className);
    }

    boolean isRealmList(ClassName className) {;
        return TypeNames.REALM_LIST.compareTo(className) == 0;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
