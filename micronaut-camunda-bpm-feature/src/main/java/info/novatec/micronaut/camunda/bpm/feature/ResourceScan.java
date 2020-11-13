package info.novatec.micronaut.camunda.bpm.feature;


import io.micronaut.core.annotation.Introspected;

import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ResourceScan {
}
