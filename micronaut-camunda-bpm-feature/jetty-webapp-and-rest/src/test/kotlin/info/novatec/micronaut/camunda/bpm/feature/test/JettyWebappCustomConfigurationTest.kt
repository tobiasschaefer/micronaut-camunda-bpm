package info.novatec.micronaut.camunda.bpm.feature.test

import info.novatec.micronaut.camunda.bpm.feature.Configuration
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

@Requires(beans = [Server::class])
@MicronautTest(propertySources = ["classpath:applicationCustomConfiguration.yml"])
class JettyWebappCustomConfigurationTest {

    @Inject
    lateinit var configuration: Configuration

    @Inject
    @field:Client("/custom-path-webapps")
    lateinit var client: RxHttpClient

    //@Test()
    fun redirectFail() {
        // FIXME Does not work?!
        val request: HttpRequest<String> = HttpRequest.GET("/")
        val res: HttpResponse<*> = client.toBlocking().exchange<String, Any>(request)
        res.headers.forEach { it -> println(it.key +" "+ it.value) }
        println(res.body())
        Assertions.assertEquals(404, res.status().code)
    }

    @Test
    fun testConfiguration() {
        Assertions.assertEquals(false, configuration.webapps.isIndexRedirectEnabled)
        Assertions.assertEquals("/custom-path-webapps", configuration.webapps.contextPath)
        Assertions.assertEquals("/custom-path-engine", configuration.rest.contextPath)
    }


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


}