package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import com.google.auto.common.MoreElements;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {

    public static Processor instance;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    // Help generate the Ruqus$$RuqusClassData file.
    private HashSet<String> realClassNames;
    private HashSet<String> queryable;
    private HashMap<String, ClassName> classMap;
    private HashMap<String, String> visibleNames;
    private HashMap<String, FieldDataBuilder> fieldData;

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

            FieldDataBuilder fdBuilder = new FieldDataBuilder();
            // Get field information for this class.
            for (Element iElement : typeElement.getEnclosedElements()) {
                // Ignore if this isn't a field.
                if (iElement.getKind() != ElementKind.FIELD) continue;

                // TODO
            }

            // Store these locally until we write out the while class data file.
            realClassNames.add(realName);
            if (isQueryable) queryable.add(realName);
            classMap.put(realName, className);
            visibleNames.put(realName, visibleName);
            fieldData.put(realName, fdBuilder);
        }

        return true;
    }

    private boolean isValidRealmClass(TypeElement classElement) {
        // Must be public.
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) return false;

        // Must not be abstract.
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) return false;

        return true;
    }


    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
