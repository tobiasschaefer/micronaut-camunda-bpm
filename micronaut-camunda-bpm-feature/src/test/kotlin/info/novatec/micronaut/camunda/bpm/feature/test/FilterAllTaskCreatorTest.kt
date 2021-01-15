package info.novatec.micronaut.camunda.bpm.feature.test

import info.novatec.micronaut.camunda.bpm.feature.Configuration
import info.novatec.micronaut.camunda.bpm.feature.FilterAllTaskCreator
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.camunda.bpm.engine.ProcessEngine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

class FilterAllTaskCreatorTest {

    @MicronautTest
    @Nested
    inner class NoFilter {

        @Inject
        lateinit var processEngine: ProcessEngine

        @Inject
        lateinit var filterAllTaskCreator: Optional<FilterAllTaskCreator>

        @Test
        fun `no filter gets created` () {
            assertFalse(filterAllTaskCreator.isPresent)
            assertEquals(0, processEngine.filterService.createFilterQuery().list().size)
        }

    }

    @MicronautTest
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Filter : TestPropertyProvider {
        override fun getProperties(): MutableMap<String, String> {
            return mutableMapOf(
                "camunda.bpm.filter.create" to "Custom Filter"
            )
        }

        @Inject
        lateinit var processEngine: ProcessEngine

        @Inject
        lateinit var filterAllTaskCreator: Optional<FilterAllTaskCreator>

        @Inject
        lateinit var configuration: Configuration

        @Test
        fun `filter created` () {
            assertTrue(filterAllTaskCreator.isPresent)
            triggerServerStartupEvent(filterAllTaskCreator.get())
            assertEquals("Custom Filter", configuration.filter.create.get())
            assertEquals("Custom Filter", processEngine.filterService.createFilterQuery().filterName("Custom Filter").singleResult().name)
            assertEquals(1, processEngine.filterService.createFilterQuery().list().size)
        }
    }

    /**
     * Provide method to trigger event manually because we don't have an application in the feature project to fire the event
     */
    fun triggerServerStartupEvent(filterAllTaskCreator: FilterAllTaskCreator) {
        filterAllTaskCreator.onApplicationEvent(ServerStartupEvent(Mockito.mock(EmbeddedServer::class.java)))
    }


}