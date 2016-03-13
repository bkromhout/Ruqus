package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import com.google.auto.service.AutoService;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class RuqusProcessor extends AbstractProcessor {

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
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
