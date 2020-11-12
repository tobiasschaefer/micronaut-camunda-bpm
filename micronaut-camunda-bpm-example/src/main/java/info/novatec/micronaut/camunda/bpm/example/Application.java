package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.feature.ResourceScan;
import io.micronaut.runtime.Micronaut;

@ResourceScan("Test")
public class Application {

    private String test;

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

}