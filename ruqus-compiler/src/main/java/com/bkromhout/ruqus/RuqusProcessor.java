package com.bkromhout.ruqus;

import com.google.auto.service.AutoService;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {
    private static boolean didGenClassData = false, didGenFieldData = false, didGenTransData = false;
    public static Processor instance;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    ClassDataBuilder classDataBuilder;
    TransformerDataBuilder transformerDataBuilder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        instance = this;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();

        classDataBuilder = new ClassDataBuilder(messager);
        transformerDataBuilder = new TransformerDataBuilder(messager);
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
        // Process classes which extend RealmModel.
        classDataBuilder.buildClassAndFieldData(roundEnv);
        // Process classes which extend RUQTransformer.
        transformerDataBuilder.buildTransformerData(roundEnv);

        // Write out all files.
        try {
            // Write out field data files.
            if (!didGenFieldData && classDataBuilder.hasFieldData()) {
                for (FieldDataBuilder fdb : classDataBuilder.getFieldDataBuilders()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Creating " + fdb.getClassName().simpleName());
                    fdb.brewJava().writeTo(filer);
                }
                didGenFieldData = true;
            }
            // Write out class data file.
            if (!didGenClassData && classDataBuilder.hasClasses()) {
                classDataBuilder.brewClassDataFile().writeTo(filer);
                didGenClassData = true;
            }
            // Write out transformers data file.
            if (!didGenTransData && transformerDataBuilder.hasClasses()) {
                transformerDataBuilder.brewTransformerDataFile().writeTo(filer);
                didGenTransData = true;
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return didGenFieldData && didGenClassData && didGenTransData;
    }
}
