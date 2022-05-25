package me.dessie.dessielib;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;
import me.dessie.dessielib.annotations.storageapi.StoredList;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Processor class for DessieLib annotations
 *
 * By utilizing the annotationProcessor, errors and warnings will
 * be thrown in some cases to help with catching development errors at compile time.
 */
@SupportedAnnotationTypes(
        {"me.dessie.dessielib.annotations.storageapi.Stored",
         "me.dessie.dessielib.annotations.storageapi.StoredList",
         "me.dessie.dessielib.annotations.storageapi.RecomposeConstructor"
        })
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class DessieLibProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(!this.claim(annotations)) return false;
        Types typeUtil = processingEnv.getTypeUtils();
        Elements elementUtil = processingEnv.getElementUtils();

        final Set<? extends Element> storedElements = roundEnv.getElementsAnnotatedWith(Stored.class);
        final Set<? extends Element> storedListElements = roundEnv.getElementsAnnotatedWith(StoredList.class);
        final Set<? extends Element> recomposeConstructElements = roundEnv.getElementsAnnotatedWith(RecomposeConstructor.class);

        for (Element element : storedElements) {
            if(element.asType().getKind() == TypeKind.ARRAY || typeUtil.isAssignable(typeUtil.erasure(element.asType()), elementUtil.getTypeElement("java.util.Collection").asType())) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Array or Collection implementation field annotated with Stored! Should be annotated with (StoredList.java:18)", element);
            }
        }

        for (Element element : storedListElements) {
            if(element.asType().getKind() != TypeKind.ARRAY && !typeUtil.isAssignable(typeUtil.erasure(element.asType()), elementUtil.getTypeElement("java.util.Collection").asType())) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Non-array or Collection field annotated with StoredList! Should be annotated with (Stored.java:22)", element);
            }
        }

        for(Element element : recomposeConstructElements) {
            List<String> annotationTypes = new ArrayList<>();
            Element enclosingElement = element.getEnclosingElement();

            if(enclosingElement instanceof TypeElement enclosingClass) {
                for(Element module : elementUtil.getAllMembers(enclosingClass)) {
                    if(module.getKind() != ElementKind.FIELD) continue;

                    if(module.getAnnotation(Stored.class) != null && module.getAnnotation(Stored.class).recompose()) {
                        annotationTypes.add(module.asType().toString());
                    } else if(module.getAnnotation(StoredList.class) != null && module.getAnnotation(StoredList.class).recompose()) {
                        annotationTypes.add(module.asType().toString());
                    }
                }
            } else return true;

            if(element instanceof ExecutableElement executableElement) {
                List<String> params = executableElement.getParameters().stream().map(e -> e.asType().toString()).toList();
                if(!annotationTypes.equals(params)) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "RecomposeConstructor parameters do not match @Stored and @StoredList fields. Make sure there are equal amounts, and the order is similar.", element);
                }
            } else return true;
        }

        return true;
    }

    private boolean claim(Set<? extends TypeElement> annotations) {
        for(TypeElement element : annotations) {
            if(this.getSupportedAnnotationTypes().contains(element.getQualifiedName().toString())) {
                return true;
            }
        }

        return false;
    }
}

