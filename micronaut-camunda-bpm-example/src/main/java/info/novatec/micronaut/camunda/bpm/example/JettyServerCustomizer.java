package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.example.rest.*;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.MalformedURLException;

/**
 * Using Micronaut Servlet with Jetty to run the REST API as a servlet.
 * https://micronaut-projects.github.io/micronaut-servlet/1.0.x/guide/#jetty
 *
 * @author Martin Sawilla
 */
@Singleton
public class JettyServerCustomizer implements BeanCreatedEventListener<Server> {

    private static final Logger log = LoggerFactory.getLogger(JettyServerCustomizer.class);



    @Override
    public Server onCreated(BeanCreatedEvent<Server> event) {


        log.info("HALLO WELT ON CREATED");
        Server jettyServer = event.getBean();

        ServletContextHandler contextHandler = (ServletContextHandler) jettyServer.getHandler();
        ServletContext context = contextHandler.getServletContext();



        //REST
        ServletContainer servletContainer = new ServletContainer(new RestApp());
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        contextHandler.addServlet(servletHolder, "/rest/*");

        log.info("REST API initialized with Micronaut Servlet - try accessing it on http://localhost:8080/rest/engine");


        /*ResourceHandler resource_handler = new ResourceHandler() {
            @Override
            public Resource getResource(String path) {
                Resource resource = Resource.newClassPathResource(path);
                if (resource == null || !resource.exists()) {
                    resource = Resource.newClassPathResource("/META-INF/resources/" + path);
                }
                return resource;
            }
        };

        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("/static/");
        jettyServer.setHandler(resource_handler);*/

        return jettyServer;
    }

    // Helper to check if it is really getting called -> It does get called!
    public static class InitListener implements ServletContextListener
    {
        private static final Logger log = LoggerFactory.getLogger(InitListener.class);
        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            sce.getServletContext().setAttribute("X-Init", "true");
            log.info("contextInitialized");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {
        }
    }
}

