package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.feature.ResourceScan;

import javax.inject.Singleton;


// TODO: Create a bean when the resourcescan is placed on the application instead: https://github.com/micronaut-projects/micronaut-core/blob/2.3.x/inject-java/src/main/java/io/micronaut/annotation/processing/BeanDefinitionInjectProcessor.java
@ResourceScan
@Singleton
public class MyResourceClass {
}
