package info.novatec.micronaut.camunda.bpm.feature;


import io.micronaut.core.annotation.Introspected;

import java.lang.annotation.*;
import java.util.List;
import java.util.Set;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Introspected
public @interface ResourceScan {

    String test = "test1235";
    String value();
}
