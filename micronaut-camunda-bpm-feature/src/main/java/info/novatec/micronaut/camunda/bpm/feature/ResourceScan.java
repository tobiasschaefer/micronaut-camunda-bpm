package info.novatec.micronaut.camunda.bpm.feature;


import java.lang.annotation.*;

/*@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented*/
/*
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Bean
@Executable
@DefaultScope(Singleton.class)
* */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
/*@Bean
@Executable
@DefaultScope(Singleton.class)*/
public @interface ResourceScan {
}
