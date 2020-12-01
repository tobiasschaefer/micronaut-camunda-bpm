package info.novatec.micronaut.camunda.bpm.example.webapps;

import org.camunda.bpm.admin.impl.web.AdminApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class AdminApp extends ResourceConfig {
    static AdminApplication adminApplication = new AdminApplication();
    public AdminApp(){
        registerClasses(adminApplication.getClasses());
    }
}
