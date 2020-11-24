package info.novatec.micronaut.camunda.bpm.example.rest;

import org.camunda.bpm.admin.impl.web.AdminApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class AdminApp extends ResourceConfig {
    static AdminApplication aA = new AdminApplication();
    public AdminApp(){
        registerClasses(aA.getClasses());
    }
}
