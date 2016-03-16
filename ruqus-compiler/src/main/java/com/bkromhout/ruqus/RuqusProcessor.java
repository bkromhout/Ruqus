package com.bkromhout.ruqus;

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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {
    private static boolean didGenClassData = false, didGenFieldDatas = false, didGenTransData = false;
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

    // Help generate the Ruqus$$RuqusTransformerData file.
    private static HashSet<String> realTClassNames;
    private static HashSet<String> realNATClassNames;
    private static HashMap<String, String> visibleTNames;
    private static HashMap<String, String> visibleNATNames;
    private static HashMap<String, Integer> numArgsMap;
    private static HashMap<String, ClassName> tClassMap;

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

        realTClassNames = new HashSet<>();
        realNATClassNames = new HashSet<>();
        visibleTNames = new HashMap<>();
        visibleNATNames = new HashMap<>();
        numArgsMap = new HashMap<>();
        tClassMap = new HashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();

        // Our annotations.
        types.add(Queryable.class.getCanonicalName());
        types.add(Hide.class.getCanonicalName());
        types.add(VisibleAs.class.getCanonicalName());
        types.add(Transformer.class.getCanonicalName());
        // Realm annotations.
        types.add(RealmClass.class.getCanonicalName());
        types.add(Ignore.class.getCanonicalName());

        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("%d", annotations.size()));
        for (TypeElement t : annotations) {
            messager.printMessage(Diagnostic.Kind.NOTE, t.toString());
        }
        // Process classes which extend RealmObject.
        buildClassAndFieldData(roundEnv);
        // Process classes which extend RUQTransformer.
        buildTransformerData(roundEnv);

        // Write out all files.
        try {
            // Write out field data files.
            if (!didGenFieldDatas && !fieldData.isEmpty()) {
                for (FieldDataBuilder fdb : fieldData.values()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Creating " + fdb.getClassName().simpleName());
                    fdb.brewJava().writeTo(filer);
                }
                didGenFieldDatas = true;
            }
            // Write out class data file.
            if (!didGenClassData && !classMap.isEmpty()) {
                brewClassDataFile().writeTo(filer);
                didGenClassData = true;
            }
            // Write out transformers data file.
            if (!didGenTransData && !tClassMap.isEmpty()) {
                brewTransformerDataFile().writeTo(filer);
                didGenTransData = true;
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return didGenFieldDatas && didGenClassData && didGenTransData;
    }

    /**
     * Process all classes which are annotated with {@link RealmClass} and get information needed to generate the class
     * data and various field data files.
     */
    private void buildClassAndFieldData(RoundEnvironment roundEnv) {
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
                error(element, "Skipping \"%s\" because there is already a realm object class called \"%s\"; Ruqus " +
                        "currently cannot handle multiple classes with the same name.", className.toString(), realName);
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
                // TODO have this generate a name from camel-case
                visibleName = realName;
            }
            // Check that visible name hasn't already been used.
            if (visibleNames.values().contains(visibleName)) {
                error(element, "Skipping \"%s\" because there is already a realm object class which has the visible " +
                        "name \"%s\"; Ruqus currently cannot handle having multiple classes with the same visible " +
                        "name.", className.toString(), visibleName);
                continue;
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
                String visibleFieldName = null;
                if (MoreElements.isAnnotationPresent(var, VisibleAs.class)) {
                    VisibleAs vaAnnot = var.getAnnotation(VisibleAs.class);
                    visibleFieldName = vaAnnot.string();
                }
                if (visibleFieldName == null || visibleFieldName.isEmpty()) visibleFieldName = realFieldName;
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

            // Store these locally until we write out the whole class data file.
            realClassNames.add(realName);
            if (isQueryable) queryable.add(realName);
            classMap.put(realName, className);
            visibleNames.put(realName, visibleName);
            fieldData.put(realName, fdBuilder);
        }
    }

    /**
     * Build the JavaFile object which will create the "Ruqus$$RuqusClassData.java" file.
     * @return JavaFile.
     */
    private JavaFile brewClassDataFile() {
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
        String addFieldDataStmt = "fieldDatas.put($S, new $T())";
        staticBlockBuilder.add("// Add field data classes.\n");
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
                TypeName fieldTypeName = TypeName.get(fieldType);
                if (isARealmObjClass(fieldTypeName)) return true;

                // RealmLists are okay as well.
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

    /**
     * Process all classes annotated with {@link Transformer} and get information needed to generate the transformer
     * data file.
     */
    private void buildTransformerData(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Transformer.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "@Transformer annotations can only be applied to classes!");
                continue;
            }
            TypeElement typeElement = MoreElements.asType(element);
            if (!isValidTransformerClass(typeElement)) continue;

            // Get ClassName object, we'll store this so that we can write out a real type later.
            ClassName className = ClassName.get(typeElement);
            // Get real class name.
            String realName = className.toString();
            // Get attributes from Transformer annotation.
            Transformer tAnnot = typeElement.getAnnotation(Transformer.class);
            String visibleName = tAnnot.name();
            Integer numArgs = tAnnot.numArgs();
            Boolean isNoArgs = tAnnot.isNoArg();

            // Ensure that visible name is non-null and non-empty.
            if (visibleName == null || visibleName.isEmpty()) {
                error(element, "Skipping \"%s\" because its @Transformer annotation is malformed; name must be " +
                        "non-null and non-empty", className.toString());
                continue;
            }

            // Ensure that numArgs is 0 if isNoArgs is true.
            if (isNoArgs && numArgs != 0) {
                error(element, "Skipping \"%s\" because its @Transformer annotation is malformed; if isNoArg is true " +
                        "then numArgs must be set to 0.", className.toString());
                continue;
            }

            // Ensure we don't have any duplicated visible names for each type of transformer.
            if (isNoArgs && visibleNATNames.values().contains(visibleName)) {
                error(element, "Skipping \"%s\" because there is already a no-args transformer class which has the " +
                        "visible name \"%s\"; Ruqus currently cannot handle having multiple no-args transformer " +
                        "classes with the same visible name.", className.toString(), visibleName);
                continue;
            } else if (visibleTNames.values().contains(visibleName)) {
                error(element, "Skipping \"%s\" because there is already a normal transformer class which has the " +
                        "visible name \"%s\"; Ruqus currently cannot handle having multiple normal transformer " +
                        "classes with the same visible name.", className.toString(), visibleName);
                continue;
            }

            // Store information about this transformer so we can write it out later.
            if (isNoArgs) {
                realNATClassNames.add(realName);
                visibleNATNames.put(realName, visibleName);
            } else {
                realTClassNames.add(realName);
                visibleTNames.put(realName, visibleName);
                numArgsMap.put(realName, numArgs);
            }
            tClassMap.put(realName, className);
        }
    }

    /**
     * Build the JavaFile object which will create the "Ruqus$$RuqusTransformerData.java" file.
     * @return JavaFile.
     */
    private JavaFile brewTransformerDataFile() {
        // Build static init block.
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        // Loop through real names.
        String addRealNameStmt = "realNames.add($S)";
        staticBlockBuilder.add("// Add real names of normal transformers.\n");
        for (String realName : realTClassNames) staticBlockBuilder.addStatement(addRealNameStmt, realName);

        // Loop through real no arg names.
        String addNARealNameStmt = "realNoArgNames.add($S)";
        staticBlockBuilder.add("// Add real names of no-args transformers.\n");
        for (String realNAName : realNATClassNames) staticBlockBuilder.addStatement(addNARealNameStmt, realNAName);

        // Loop through visible names.
        String addVisibleNameStmt = "visibleNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names of normal transformers.\n");
        for (Map.Entry<String, String> entry : visibleTNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNameStmt, entry.getKey(), entry.getValue());

        // Loop through visible no arg names.
        String addVisibleNANameStmt = "visibleNoArgNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names of no-arg transformers.\n");
        for (Map.Entry<String, String> entry : visibleNATNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNANameStmt, entry.getKey(), entry.getValue());

        // Loop through number of arguments values.
        String addNumArgsStmt = "numArgs.put($S, $L)";
        staticBlockBuilder.add("// Add number of arguments values.\n");
        for (Map.Entry<String, Integer> entry : numArgsMap.entrySet())
            staticBlockBuilder.addStatement(addNumArgsStmt, entry.getKey(), entry.getValue());

        // Loop through transformer class object names.
        String addClassStmt = "classMap.put($S, $T.class)";
        staticBlockBuilder.add("// Add transformer class objects.\n");
        for (Map.Entry<String, ClassName> entry : tClassMap.entrySet())
            staticBlockBuilder.addStatement(addClassStmt, entry.getKey(), entry.getValue());

        // Finally, build this code block.
        CodeBlock staticBlock = staticBlockBuilder.build();

        // Build class.
        TypeSpec clazz = TypeSpec.classBuilder(C.GEN_TRANSFORMER_DATA_CLASS_NAME)
                                 .superclass(TypeNames.TRANS_DATA_CLASS)
                                 .addModifiers(Modifier.FINAL)
                                 .addStaticBlock(staticBlock)
                                 .build();

        // Build file and return it.
        return JavaFile.builder(C.GEN_PKG, clazz)
                       .addFileComment(C.GEN_CODE_FILE_COMMENT)
                       .build();
    }

    private boolean isValidTransformerClass(TypeElement classElement) {
        // Must be public and non-abstract.
        boolean validMods = classElement.getModifiers().contains(Modifier.PUBLIC) &&
                !classElement.getModifiers().contains(Modifier.ABSTRACT);
        if (isSubtypeOfType(classElement.asType(), TypeNames.RUQ_TRANS_CLASS.toString())) return validMods;
        else {
            error(classElement, "Skipping \"%s\" because transformer classes must extend (either directly or " +
                    "indirectly) %s.", ClassName.get(classElement).toString(), TypeNames.RUQ_TRANS_CLASS.toString());
            return false;
        }
    }

    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
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

    static boolean isARealmObjClass(TypeName className) {
        return classMap.values().contains(className);
    }

    boolean isRealmList(TypeName className) {
        return className != null && className.toString().contains(TypeNames.REALM_LIST.simpleName());
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
