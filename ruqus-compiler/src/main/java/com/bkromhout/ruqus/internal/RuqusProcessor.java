package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import com.google.auto.service.AutoService;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {
    enum Round {
        FIRST, SECOND
    }

    public static Processor instance;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;
    private Round currentRound = null;

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
        if (currentRound == null) currentRound = Round.FIRST;
        else if (currentRound == Round.FIRST) currentRound = Round.SECOND;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
