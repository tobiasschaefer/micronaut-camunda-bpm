package info.novatec.micronaut.camunda.bpm.feature.webapp;

import org.camunda.bpm.cockpit.impl.web.CockpitApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class CockpitApp extends ResourceConfig {

    static CockpitApplication cockpitApplication = new CockpitApplication();

    public CockpitApp() {
        registerClasses(cockpitApplication.getClasses());
        // Disable WADL-Feature because we do not want to expose a XML description of our RESTful web application.
        property("jersey.config.server.wadl.disableWadl", "true");
    }
}
