package com.bkromhout.ruqus;

import com.google.auto.common.MoreElements;
import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.*;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Processes all classes annotated with {@link Transformer} in order to generate a file with information about all
 * transformers for Ruqus to use at runtime (instead of doing a ton of reflection).
 */
public class TransformerDataBuilder {

    private Messager messager;
    private static HashSet<String> realClassNames;
    private static HashSet<String> realNAClassNames;
    private static HashMap<String, String> visibleNames;
    private static HashMap<String, String> visibleNANames;
    private static HashMap<String, Integer> numArgsMap;
    private static HashMap<String, ClassName> classMap;
    private static HashMap<ClassName, HashSet<String>> typesMap;

    TransformerDataBuilder(Messager messager) {
        this.messager = messager;
        realClassNames = new HashSet<>();
        realNAClassNames = new HashSet<>();
        visibleNames = new HashMap<>();
        visibleNANames = new HashMap<>();
        numArgsMap = new HashMap<>();
        classMap = new HashMap<>();
        typesMap = new HashMap<>();
    }

    boolean hasClasses() {
        return !classMap.isEmpty();
    }

    /**
     * Process all classes annotated with {@link Transformer} and get information needed to generate the transformer
     * data file.
     */
    void buildTransformerData(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Transformer.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "@Transformer annotations can only be applied to classes!");
                continue;
            }

            TypeElement typeElement = MoreElements.asType(element);
            // Get ClassName object, we'll store this so that we can write out a real type later.
            ClassName className = ClassName.get(typeElement);
            // Get real class name.
            String realName = className.toString();
            // Get attributes from Transformer annotation.
            Transformer tAnnot = typeElement.getAnnotation(Transformer.class);
            String visibleName = tAnnot.name();
            Class[] validTypes = tAnnot.validArgTypes();
            Integer numArgs = tAnnot.numArgs();
            Boolean isNoArgs = tAnnot.isNoArgs();

            // Validate this transformer class.
            if (!isValidTransformerClass(typeElement, className, visibleName, validTypes, numArgs, isNoArgs)) continue;

            // Store information about this transformer so we can write it out later.
            if (isNoArgs) {
                realNAClassNames.add(realName);
                visibleNANames.put(realName, visibleName);
            } else {
                realClassNames.add(realName);
                visibleNames.put(realName, visibleName);
                numArgsMap.put(realName, numArgs);
                processValidTypes(realName, validTypes);
            }
            classMap.put(realName, className);
        }
    }

    private void processValidTypes(String realName, Class[] validTypes) {
        for (Class type : validTypes) {
            ClassName typeName = ClassName.get(type);
            if (!typesMap.containsKey(typeName)) typesMap.put(typeName, new HashSet<String>());
            typesMap.get(typeName).add(realName);
        }
    }

    private boolean isValidTransformerClass(TypeElement element, ClassName className, String visibleName,
                                            Class[] validTypes, Integer numArgs, Boolean isNoArgs) {
        // Must extend (directly or indirectly) RUQTransformer.
        if (!isSubtypeOfType(element.asType(), TypeNames.RUQ_TRANS_CLASS.toString())) {
            error(element, "Skipping \"%s\" because transformer classes must extend (either directly or " +
                            "indirectly) %s, but instead it extends %s.", ClassName.get(element).toString(),
                    TypeNames.RUQ_TRANS_CLASS.toString(), element.getSuperclass().toString());
            return false;
        }

        // Ensure that visible name is non-null and non-empty.
        if (visibleName == null || visibleName.isEmpty()) {
            error(element, "Skipping \"%s\" because its @Transformer annotation is malformed; name must be " +
                    "non-null and non-empty", className.toString());
            return false;
        }
        // If this isn't a no-args transformer and numArgs > 0, ensure that we have an array of types.
        if (!isNoArgs && numArgs != 0 && (validTypes == null || validTypes.length == 0)) {
            error(element, "Skipping \"%s\" because its @Transformer annotation is malformed; the validArgTypes " +
                            "array must be non-null and non-empty if isNoArgs = false and numArgs > 0.",
                    className.toString());
            return false;
        }
        // Ensure we don't have any duplicated visible names for each type of transformer.
        if (isNoArgs && visibleNANames.values().contains(visibleName)) {
            error(element, "Skipping \"%s\" because there is already a no-args transformer class which has the " +
                    "visible name \"%s\"; Ruqus currently cannot handle having multiple no-args transformer " +
                    "classes with the same visible name.", className.toString(), visibleName);
            return false;
        } else if (visibleNames.values().contains(visibleName)) {
            error(element, "Skipping \"%s\" because there is already a normal transformer class which has the " +
                    "visible name \"%s\"; Ruqus currently cannot handle having multiple normal transformer " +
                    "classes with the same visible name.", className.toString(), visibleName);
            return false;
        }

        // Must be public and non-abstract.
        return element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.ABSTRACT);
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

    /**
     * Build the JavaFile object which will create the "Ruqus$$RuqusTransformerData.java" file.
     * @return JavaFile.
     */
    JavaFile brewTransformerDataFile() {
        String genClassName = nextTDataClassName();
        ClassName genClassType = ClassName.get(C.GEN_PKG, genClassName);

        // Build static instance var.
        FieldSpec instanceField = FieldSpec.builder(TypeNames.TRANS_DATA_CLASS, "INSTANCE",
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                           .initializer("new $T()", genClassType)
                                           .build();

        // Build static init block.
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        // Loop through real names.
        String addRealNameStmt = "realNames.add($S)";
        staticBlockBuilder.add("// Add real names of normal transformers.\n");
        for (String realName : realClassNames) staticBlockBuilder.addStatement(addRealNameStmt, realName);

        // Loop through real no arg names.
        String addNARealNameStmt = "realNoArgNames.add($S)";
        staticBlockBuilder.add("// Add real names of no-args transformers.\n");
        for (String realNAName : realNAClassNames) staticBlockBuilder.addStatement(addNARealNameStmt, realNAName);

        // Loop through visible names.
        String addVisibleNameStmt = "visibleNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names of normal transformers.\n");
        for (Map.Entry<String, String> entry : visibleNames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNameStmt, entry.getKey(), entry.getValue());

        // Loop through visible no arg names.
        String addVisibleNANameStmt = "visibleNoArgNames.put($S, $S)";
        staticBlockBuilder.add("// Add visible names of no-arg transformers.\n");
        for (Map.Entry<String, String> entry : visibleNANames.entrySet())
            staticBlockBuilder.addStatement(addVisibleNANameStmt, entry.getKey(), entry.getValue());

        // Loop through number of arguments values.
        String addNumArgsStmt = "numArgs.put($S, $L)";
        staticBlockBuilder.add("// Add number of arguments values.\n");
        for (Map.Entry<String, Integer> entry : numArgsMap.entrySet())
            staticBlockBuilder.addStatement(addNumArgsStmt, entry.getKey(), entry.getValue());

        // Loop through transformer class object names.
        String addClassStmt = "classMap.put($S, $T.class)";
        staticBlockBuilder.add("// Add transformer class objects.\n");
        for (Map.Entry<String, ClassName> entry : classMap.entrySet())
            staticBlockBuilder.addStatement(addClassStmt, entry.getKey(), entry.getValue());

        // Loop through valid types map.
        staticBlockBuilder.add("// Map types to the transformers which accept them.\n");
        for (Map.Entry<ClassName, HashSet<String>> entry : typesMap.entrySet()) {
            staticBlockBuilder.add("typesToNames.put($T.class, new HashSet<String>() {{\n", entry.getKey())
                              .indent();
            // Add the names of transformers for this type.
            for (String s : entry.getValue()) staticBlockBuilder.addStatement("add($S)", s);
            staticBlockBuilder.unindent()
                              .add("}});\n");
        }

        // Finally, build this code block.
        CodeBlock staticBlock = staticBlockBuilder.build();

        // Build class.
        TypeSpec clazz = TypeSpec.classBuilder(genClassName)
                                 .superclass(TypeNames.TRANS_DATA_CLASS)
                                 .addModifiers(Modifier.FINAL)
                                 .addField(instanceField)
                                 .addStaticBlock(staticBlock)
                                 .build();

        typesMap.put(genClassType, new HashSet<String>() {{
            add("Test");
        }});

        // Build file and return it.
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating " + genClassType.simpleName());
        return JavaFile.builder(C.GEN_PKG, clazz)
                       .addFileComment(C.GEN_CODE_FILE_COMMENT)
                       .build();
    }

    private String nextTDataClassName() {
        // Figure out what number we'll need to append to the end of the class name.
        String base = C.GEN_PKG_PREFIX + C.GEN_TRANSFORMER_DATA_CLASS_NAME;
        int num = 1;
        while (true) {
            try {
                Class.forName(base + String.valueOf(num));
            } catch (ClassNotFoundException e) {
                // This class isn't already taken, so we can generate it. Break out of this loop.
                return C.GEN_TRANSFORMER_DATA_CLASS_NAME + String.valueOf(num);
            }
            num++;
        }
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
