package info.novatec.micronaut.camunda.bpm.feature.test

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.eclipse.jetty.server.Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

/**
 * Simple Test to check if the Webapps run.
 *
 * @author Martin Sawilla
 */
@MicronautTest
@Requires(beans = [Server::class])
class JettyWebappTest {

    @Inject
    @field:Client("/camunda")
    lateinit var client: RxHttpClient

    @Test
    fun welcome() {
        val request: HttpRequest<String> = HttpRequest.GET("/app/welcome/default")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        Assertions.assertEquals(200, res.status().code)
    }

    @Test
    fun admin() {
        val request: HttpRequest<String> = HttpRequest.GET("/app/admin/default")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        Assertions.assertEquals(200, res.status().code)
    }

    @Test
    fun cockpit() {
        val request: HttpRequest<String> = HttpRequest.GET("/app/cockpit/default")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        Assertions.assertEquals(200, res.status().code)
    }

    @Test
    fun tasklist() {
        val request: HttpRequest<String> = HttpRequest.GET("/app/tasklist/default")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        Assertions.assertEquals(200, res.status().code)
    }

    @Test()
    fun redirect() {
        val request: HttpRequest<String> = HttpRequest.GET("/")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        Assertions.assertEquals(200, res.status().code)
        Assertions.assertEquals("text/html", res.header("Content-Type"))
    }
}
