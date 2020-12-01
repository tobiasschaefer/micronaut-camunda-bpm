package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.example.webapps.*;
import info.novatec.micronaut.camunda.bpm.example.rest.RestApp;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.camunda.bpm.admin.impl.web.bootstrap.AdminContainerBootstrap;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.web.bootstrap.CockpitContainerBootstrap;
import org.camunda.bpm.engine.rest.filter.CacheControlFilter;
import org.camunda.bpm.engine.rest.filter.EmptyBodyFilter;
import org.camunda.bpm.tasklist.impl.web.bootstrap.TasklistContainerBootstrap;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.filter.headersec.HttpHeaderSecurityFilter;
import org.camunda.bpm.webapp.impl.security.filter.util.HttpSessionMutexListener;
import org.camunda.bpm.welcome.impl.web.bootstrap.WelcomeContainerBootstrap;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.*;
import java.util.Collections;
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


        log.info("HALLO WELT ON CREATED");
        Server jettyServer = event.getBean();

        ServletContextHandler contextHandler = (ServletContextHandler) jettyServer.getHandler();
        ServletContext context = contextHandler.getServletContext();

        //REST
        ServletContainer servletContainer = new ServletContainer(new RestApp());
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        contextHandler.addServlet(servletHolder, "/rest/*");


        contextHandler.addEventListener(new CockpitContainerBootstrap());
        contextHandler.addEventListener(new AdminContainerBootstrap());
        contextHandler.addEventListener(new TasklistContainerBootstrap());
        contextHandler.addEventListener(new WelcomeContainerBootstrap());
        contextHandler.addEventListener(new HttpSessionMutexListener());

        contextHandler.addEventListener(new InitListener());

        log.info("REST API initialized with Micronaut Servlet - try accessing it on http://localhost:8080/rest/engine");


        // DEF. The servlet dispatcher allows a request to travel from one servlet to other servlets
        EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR);

        /*contextHandler.addFilter(ProcessEnginesFilter.class, "/app/*", DISPATCHER_TYPES);
        contextHandler.addFilter(AuthenticationFilter.class, "/app/*", DISPATCHER_TYPES);
        contextHandler.addFilter(HttpHeaderSecurityFilter.class, "/app/*", DISPATCHER_TYPES);
        contextHandler.addFilter(EmptyBodyFilter.class, "/app/*", DISPATCHER_TYPES);
        contextHandler.addFilter(CacheControlFilter.class, "/app/*", DISPATCHER_TYPES);*/
        // Authentication uses a Session Cookie!
        SessionIdManager idManager = new DefaultSessionIdManager(jettyServer);
        jettyServer.setSessionIdManager(idManager);
        SessionHandler sessionHandler = new SessionHandler();
        contextHandler.setSessionHandler(sessionHandler);

        return jettyServer;
    }

    // I need to configure the Camunda Webapps here because in the onCreated method
    // I do not have access to e.g. Cockpit.getRuntimeDelegate() (results in null)
    // But here I the getRuntimeDelegate is available and so I do not get errors with my servlet
    public static class InitListener implements ServletContextListener
    {
        private static final Logger log = LoggerFactory.getLogger(InitListener.class);

        private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR);

        private ServletContext servletContext;

        private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final String... urlPatterns) {
            FilterRegistration filterRegistration = servletContext.getFilterRegistration(filterName);
            if (filterRegistration == null) {
                filterRegistration = servletContext.addFilter(filterName, filterClass);
                filterRegistration.addMappingForUrlPatterns(DISPATCHER_TYPES, true, urlPatterns);
                log.info("Filter {} for URL {} registered", filterName, urlPatterns);
            }
            return filterRegistration;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            this.servletContext = sce.getServletContext();

            servletContext.addServlet("CockpitApp", new ServletContainer(new CockpitApp())).addMapping("/api/cockpit/*");
            servletContext.addServlet("AdminApp", new ServletContainer(new AdminApp())).addMapping("/api/admin/*");
            servletContext.addServlet("TasklistApp", new ServletContainer(new TasklistApp())).addMapping("/api/tasklist/*");
            servletContext.addServlet("EngineRestApp", new ServletContainer(new EngineRestApp())).addMapping("/api/engine/*");
            servletContext.addServlet("WelcomeApp", new ServletContainer(new WelcomeApp())).addMapping("/api/welcome/*");
            log.info("In theory: Servlets are initialized");
            registerFilter("ProcessEnginesFilter", ProcessEnginesFilter.class, "/api/*", "/app/*");
            registerFilter("AuthenticationFilter", AuthenticationFilter.class, "/api/*", "/app/*");
            registerFilter("HttpHeaderSecurityFilter", HttpHeaderSecurityFilter.class, "/api/*", "/app/*");
            registerFilter("EmptyBodyFilter", EmptyBodyFilter.class, "/api/*", "/app/*");
            registerFilter("CacheControlFilter", CacheControlFilter.class, "/api/*", "/app/*");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {
        }
    }

    /*public static class TestFilter implements Filter {
        private static final Logger log = LoggerFactory.getLogger(TestFilter.class);
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            log.info(request.toString());
            chain.doFilter(request, response);
        }

    }*/
}

