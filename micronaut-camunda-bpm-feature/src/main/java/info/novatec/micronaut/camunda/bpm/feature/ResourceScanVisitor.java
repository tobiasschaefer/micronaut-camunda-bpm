package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ResourceScanVisitor implements TypeElementVisitor<ResourceScan, Object> {

    private static final Logger log = LoggerFactory.getLogger(ResourceScanVisitor.class);

    @Override
    public void start(VisitorContext visitorContext) {
        log.info("Test1");
        log.debug("Test2");
        log.error("Test3");
        System.err.println("Test4");

        throw new RuntimeException();
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
       String test = "test";

       log.info("Test1");
       log.debug("Test2");
       log.error("Test3");
       System.err.println("Test4");

       throw new RuntimeException();

           /*
        DO SCANNING HERE?
         */

           //https://www.slideshare.net/graemerocher/micronaut-deep-dive-devoxx-belgium-2019 (Folie 36)
       /* element.annotate(ResourceScan.class, resourceScanAnnotationValueBuilder -> {
            element.stringValue(ResourceScan.class,test)
                    .ifPresent(resourceScanAnnotationValueBuilder::value);
        });*/

        }
}


