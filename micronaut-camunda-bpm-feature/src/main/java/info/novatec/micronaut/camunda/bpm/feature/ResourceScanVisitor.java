package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


public class ResourceScanVisitor implements TypeElementVisitor<ResourceScan, Object> {

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
       String test = "test";

           /*
        DO SCANNING HERE?
         */
           //RuntimeExeption oder log
           //https://www.slideshare.net/graemerocher/micronaut-deep-dive-devoxx-belgium-2019 (Folie 36)
        element.annotate(ResourceScan.class, resourceScanAnnotationValueBuilder -> {
            element.stringValue(ResourceScan.class,test)
                    .ifPresent(resourceScanAnnotationValueBuilder::value);
        });

        }
}


