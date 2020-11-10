package info.novatec.micronaut.camunda.bpm.example.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.web.CockpitApplication;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.glassfish.jersey.server.ResourceConfig;
import org.camunda.bpm.welcome.impl.web.WelcomeApplication;
import java.util.List;
import java.util.Set;

public class WelcomeApp extends ResourceConfig {
    public WelcomeApp() {
        // Class to check if everything works properly
        register(HelloWorldResource.class);
        //CockpitApplication wa = new CockpitApplication();
        //registerClasses(wa.getClasses());
        register(JacksonConfigurator.class);
        register(JacksonJsonProvider.class);
        register(ExceptionHandler.class);
        register(RestExceptionHandler.class);

        /*List<CockpitPlugin> plugins = getCockpitPlugins();

        for (CockpitPlugin plugin : plugins) {
            registerClasses(plugin.getResourceClasses());
        }*/

    }

    private List<CockpitPlugin> getCockpitPlugins() {
        return Cockpit.getRuntimeDelegate().getAppPluginRegistry().getPlugins();
    }
}
