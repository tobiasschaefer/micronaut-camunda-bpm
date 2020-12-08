package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.example.webapps.*;
import info.novatec.micronaut.camunda.bpm.example.rest.RestApp;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.camunda.bpm.admin.impl.web.bootstrap.AdminContainerBootstrap;
import org.camunda.bpm.cockpit.impl.web.bootstrap.CockpitContainerBootstrap;
import org.camunda.bpm.engine.rest.filter.CacheControlFilter;
import org.camunda.bpm.engine.rest.filter.EmptyBodyFilter;
import org.camunda.bpm.tasklist.impl.web.bootstrap.TasklistContainerBootstrap;
import org.camunda.bpm.webapp.impl.engine.ProcessEnginesFilter;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.filter.headersec.HttpHeaderSecurityFilter;
import org.camunda.bpm.webapp.impl.security.filter.util.HttpSessionMutexListener;
import org.camunda.bpm.welcome.impl.web.bootstrap.WelcomeContainerBootstrap;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.*;
import java.util.EnumSet;

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

        Server jettyServer = event.getBean();

        ServletContextHandler contextHandler = (ServletContextHandler) jettyServer.getHandler();
        contextHandler.setContextPath("/*");
        // REST
        ServletContextHandler restServletContextHandler = new ServletContextHandler();
        restServletContextHandler.setContextPath("/engine-rest");
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(new RestApp()));
        restServletContextHandler.addServlet(servletHolder, "/*");

        log.info("REST API initialized, try to access it on {}:8080/engine-rest/engine", jettyServer.getURI().toString());

        // WEBAPPS
        ResourceHandler webappsResourceHandler = new ResourceHandler();
        webappsResourceHandler.setDirectoriesListed(false);

        Resource webappsResource = Resource.newClassPathResource("/META-INF/resources/webjars/camunda");
        Resource pluginsResource = Resource.newClassPathResource("/META-INF/resources");
        ResourceCollection resources = new ResourceCollection(webappsResource, pluginsResource);
        //In tutorial that was a ContextHandler
        ServletContextHandler webappsContextHandler = new ServletContextHandler();
        webappsContextHandler.setContextPath("/camunda");
        webappsContextHandler.setBaseResource(resources);
        webappsContextHandler.insertHandler(webappsResourceHandler);

        webappsContextHandler.addEventListener(new CockpitContainerBootstrap());
        webappsContextHandler.addEventListener(new AdminContainerBootstrap());
        webappsContextHandler.addEventListener(new TasklistContainerBootstrap());
        webappsContextHandler.addEventListener(new WelcomeContainerBootstrap());
        webappsContextHandler.addEventListener(new HttpSessionMutexListener());
        webappsContextHandler.addEventListener(new ServletContextInitializedListener());

        log.info("Access the WEBAPPS here {}:{}/camunda/app/welcome/default", jettyServer.getURI().toString(), jettyServer.getURI().getPort());

        //Registers SessionTrackingMode.COOKIE, SessionTrackingMode.URL <- I need Cookie
        SessionHandler sessionHandler = new SessionHandler();
        webappsContextHandler.setSessionHandler(sessionHandler);

        /* For OLD REGISTRATION on defaultServlet*/
        /*
        contextHandler.addEventListener(new CockpitContainerBootstrap());
        contextHandler.addEventListener(new AdminContainerBootstrap());
        contextHandler.addEventListener(new TasklistContainerBootstrap());
        contextHandler.addEventListener(new WelcomeContainerBootstrap());
        contextHandler.addEventListener(new HttpSessionMutexListener());
        contextHandler.addEventListener(new ServletContextInitializedListener());
        SessionHandler sessionHandler = new SessionHandler();
        contextHandler.setSessionHandler(sessionHandler);
        */


        // Multiple handlers for same path => Check which matches first
        ContextHandlerCollection contexts = new ContextHandlerCollection(contextHandler, webappsContextHandler,restServletContextHandler);
        jettyServer.setHandler(contexts);

        return jettyServer;
    }

    /*  I need to configure the Camunda Webapps here because in the JettyServerCustomizer.onCreated() method because
    I do not have access to e.g. Cockpit.getRuntimeDelegate() (results in null). But here the Cockpit.getRuntimeDelegate() does not return null.
     */
    public static class ServletContextInitializedListener implements ServletContextListener {
        private static final Logger log = LoggerFactory.getLogger(ServletContextInitializedListener.class);

        private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR);

        private static ServletContext servletContext;

        private void registerFilter(final String filterName, final Class<? extends Filter> filterClass, final String... urlPatterns) {
            FilterRegistration filterRegistration = servletContext.getFilterRegistration(filterName);
            if (filterRegistration == null) {
                filterRegistration = servletContext.addFilter(filterName, filterClass);
                filterRegistration.addMappingForUrlPatterns(DISPATCHER_TYPES, true, urlPatterns);
                log.info("Filter {} for URL {} registered", filterName, urlPatterns);
            }
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            servletContext = sce.getServletContext();

            servletContext.addServlet("CockpitApp", new ServletContainer(new CockpitApp())).addMapping("/api/cockpit/*");
            servletContext.addServlet("AdminApp", new ServletContainer(new AdminApp())).addMapping("/api/admin/*");
            servletContext.addServlet("TasklistApp", new ServletContainer(new TasklistApp())).addMapping("/api/tasklist/*");
            servletContext.addServlet("EngineRestApp", new ServletContainer(new EngineRestApp())).addMapping("/api/engine/*");
            servletContext.addServlet("WelcomeApp", new ServletContainer(new WelcomeApp())).addMapping("/api/welcome/*");
            log.info("Webapps Servlets registered");
            registerFilter("ProcessEnginesFilter", ProcessEnginesFilter.class, "/api/*", "/app/*");
            registerFilter("AuthenticationFilter", AuthenticationFilter.class, "/api/*", "/app/*");
            registerFilter("HttpHeaderSecurityFilter", HttpHeaderSecurityFilter.class, "/api/*", "/app/*");
            registerFilter("EmptyBodyFilter", EmptyBodyFilter.class, "/api/*", "/app/*");
            registerFilter("CacheControlFilter", CacheControlFilter.class, "/api/*", "/app/*");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
        }
    }
}

