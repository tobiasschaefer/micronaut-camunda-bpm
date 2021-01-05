package info.novatec.micronaut.camunda.bpm.feature.test

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.eclipse.jetty.server.Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@Requires(beans = [Server::class])
@MicronautTest(propertySources = ["classpath:applicationCustomConfiguration.yml"])
class JettyRestCustomConfigurationTest {

    @Inject
    @field:Client("/custom-path-engine")
    lateinit var client: RxHttpClient

    @Test
    fun engine() {
        val request: HttpRequest<String> = HttpRequest.GET("/engine")
        val body = client.toBlocking().retrieve(request)

        Assertions.assertEquals("""[{"name":"default"}]""", body)
    }
}