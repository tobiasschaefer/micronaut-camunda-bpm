package info.novatec.micronaut.camunda.bpm.example.webapps;

import org.camunda.bpm.webapp.impl.engine.EngineRestApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class EngineRestApp extends ResourceConfig {
    static EngineRestApplication eA = new EngineRestApplication();
    public EngineRestApp() {
        registerClasses(eA.getClasses());
    }
}
