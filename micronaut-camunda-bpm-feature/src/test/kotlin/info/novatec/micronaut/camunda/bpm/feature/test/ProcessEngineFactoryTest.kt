package info.novatec.micronaut.camunda.bpm.feature.test

import info.novatec.micronaut.camunda.bpm.feature.ProcessEngineFactory
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.camunda.bpm.engine.ProcessEngine
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class ProcessEngineFactoryTest {
    @Inject
    lateinit var processEngine: ProcessEngine

    @Test
    fun processEngineAvailableInApplicationContext() {
        assertNotNull(processEngine)
    }

    @Test
    fun testDeploymentName() {
        assertEquals(ProcessEngineFactory.MICRONAUT_AUTO_DEPLOYMENT_NAME, processEngine.repositoryService.createDeploymentQuery().singleResult().name)
    }

    @Test
    fun testConfigurationCustomizer() {
        assertEquals(MicronautProcessEngineConfigurationCustomizer.PROCESS_ENGINE_NAME, processEngine.name)
    }
}