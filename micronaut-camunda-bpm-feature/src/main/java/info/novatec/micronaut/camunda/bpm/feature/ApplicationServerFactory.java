package info.novatec.micronaut.camunda.bpm.feature;


import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.servlet.jetty.JettyServer;
import io.micronaut.servlet.tomcat.TomcatServer;
import io.micronaut.servlet.undertow.UndertowServer;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

/**
 * Bean factory for {@link ApplicationServer} containing the embedded server version.
 * <p>
 * Note: We're not using javax.servlet.ServletContainerInitializer to not rely on micronaut-servlet and therefore
 * minimize dependencies.
 *
 * @author Titus Meyer
 */
@Factory
public class ApplicationServerFactory {
    protected final Optional<EmbeddedServer> embeddedServer;

    ApplicationServerFactory(Optional<EmbeddedServer> embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

    @Singleton
    @Requires(classes = {io.micronaut.http.server.netty.NettyHttpServer.class, io.netty.util.Version.class})
    public ApplicationServer nettyServerInfo() {
        assertEmbeddedServerIsActive(NettyHttpServer.class);
        if (io.netty.util.Version.identify().size() > 0) {
            Map.Entry<String, io.netty.util.Version> version = io.netty.util.Version.identify().entrySet().iterator().next();
            return new ApplicationServer(version.toString().replaceFirst("-.+-", "-"));
        } else {
            throw new DisabledBeanException("Version information is not available for Netty.");
        }
    }

    @Singleton
    @Requires(classes = {JettyServer.class, org.eclipse.jetty.util.Jetty.class})
    public ApplicationServer jettyServerInfo() {
        assertEmbeddedServerIsActive(JettyServer.class);
        return new ApplicationServer("jetty/" + org.eclipse.jetty.util.Jetty.VERSION);
    }

    @Singleton
    @Requires(classes = {TomcatServer.class, org.apache.catalina.util.ServerInfo.class})
    public ApplicationServer tomcatServerInfo() {
        assertEmbeddedServerIsActive(TomcatServer.class);
        return new ApplicationServer(org.apache.catalina.util.ServerInfo.getServerInfo());
    }

    @Singleton
    @Requires(classes = {UndertowServer.class, io.undertow.Version.class})
    public ApplicationServer undertowServerInfo() {
        assertEmbeddedServerIsActive(UndertowServer.class);
        return new ApplicationServer(io.undertow.Version.getFullVersionString());
    }

    protected void assertEmbeddedServerIsActive(Class<?> clazz) {
        if (!embeddedServer.isPresent() || !embeddedServer.get().isServer() || !clazz.isAssignableFrom(embeddedServer.get().getClass())) {
            throw new DisabledBeanException(clazz.getName() + " is in classpath but not the active embedded server!");
        }
    }
}
