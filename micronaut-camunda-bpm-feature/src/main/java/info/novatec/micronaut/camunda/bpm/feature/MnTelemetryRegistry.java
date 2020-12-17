package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.runtime.server.EmbeddedServer;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;

import javax.inject.Singleton;

/**
 * Micronaut specific implementation of {@link TelemetryRegistry}.
 *
 * @author Tobias Schäfer
 */
@Singleton
public class MnTelemetryRegistry extends TelemetryRegistry {

    protected static final String INTEGRATION_NAME = "micronaut-camunda-bpm";

    public MnTelemetryRegistry(EmbeddedServer embeddedServer) {
        /*if (embeddedServer instanceof JettyServer) {
            setApplicationServer("jetty/" + Jetty.VERSION);
        } else*/ if (embeddedServer instanceof NettyHttpServer) {
            io.netty.util.Version.identify().get()
            setApplicationServer("netty/");
        }
    }

    @Override
    public String getCamundaIntegration() {
        return INTEGRATION_NAME;
    }
}
