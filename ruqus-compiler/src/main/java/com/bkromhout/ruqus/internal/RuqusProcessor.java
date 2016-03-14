package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
    private HashSet<String> queryable = new HashSet<>();
    private HashMap<String, ClassName> classMap = new HashMap<>();
    private HashMap<String, String> visibleNames = new HashMap<>();

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
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "RealmClass annotations can only be applied to classes!");
                continue;
            }

            // TODO get class info and fields info.
        }

        return true;
    }


    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
