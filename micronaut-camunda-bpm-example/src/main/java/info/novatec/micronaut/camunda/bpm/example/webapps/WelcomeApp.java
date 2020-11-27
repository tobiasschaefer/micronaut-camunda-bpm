package info.novatec.micronaut.camunda.bpm.example.webapps;

import org.camunda.bpm.welcome.impl.web.WelcomeApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class WelcomeApp extends ResourceConfig {

    static WelcomeApplication wA = new WelcomeApplication();

    public WelcomeApp() {
        registerClasses(wA.getClasses());
    }

}
