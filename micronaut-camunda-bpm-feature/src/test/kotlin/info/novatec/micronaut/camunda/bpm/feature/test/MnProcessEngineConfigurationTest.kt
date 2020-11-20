package info.novatec.micronaut.camunda.bpm.feature.test

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.transaction.TransactionCallback
import io.micronaut.transaction.TransactionStatus
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import javax.inject.Inject

/**
 * Executes the inherited tests with a transaction manager.
 *
 * @author Lukasz Frankowski
 * @author Tobias Schäfer
 */
@MicronautTest(transactional = false)
open class MnProcessEngineConfigurationTest {
    @Inject
    lateinit var runtimeService: RuntimeService

    @Inject
    lateinit var historyService: HistoryService

    @Inject
    lateinit var transactionManager: SynchronousTransactionManager<Connection>

    @Test
    open fun testCommit() {
        startProcess(WITH_COMMIT)
        // process has been finished with commit and we can find it in the history
        assertEquals(1, findHistoricProcessInstances(WITH_COMMIT).size)
    }

    @Test
    open fun testRollback() {
        assertThrows(RuntimeException::class.java) { startProcessWithRuntimeError(WITH_ROLLBACK) }
        // process has been finished but rollback happened and we cannot find it in the history
        assertEquals(0, findHistoricProcessInstances(WITH_ROLLBACK).size)
    }

    @Test
    open fun testSurroundingTransactionWithCommit() {
        transactionManager.executeWrite { transactionStatus: TransactionStatus<Connection?> ->
            try {
                startProcess(TX_WITH_COMMIT)
                return@executeWrite startProcess(TX_WITH_COMMIT)
            } finally {
                assertFalse(transactionStatus.isRollbackOnly)
            }
        }
        // both processes have been finished with commit and we can find them in the history
        assertEquals(2, findHistoricProcessInstances(TX_WITH_COMMIT).size)
    }

    @Test
    open fun testSurroundingTransactionWithRollback() {
        assertThrows(RuntimeException::class.java) {
            transactionManager.executeWrite(TransactionCallback<Connection, String> { transactionStatus: TransactionStatus<Connection> ->
                try {
                    startProcess(TX_WITH_ROLLBACK)
                    return@TransactionCallback startProcessWithRuntimeError(TX_WITH_ROLLBACK)
                } finally {
                    assertTrue(transactionStatus.isRollbackOnly)
                }
            })
        }
        // first process has finished successfully but rollback happened and we cannot find anything in the history
        assertEquals(0, findHistoricProcessInstances(TX_WITH_ROLLBACK).size)
    }

    @Test
    open fun testNoSurroundingTransactionWithCommits() {
        startProcess(WITH_COMMIT_COMMIT)
        startProcess(WITH_COMMIT_COMMIT)
        // both processes have been finished with commit and we can find them in the history
        assertEquals(2, findHistoricProcessInstances(WITH_COMMIT_COMMIT).size)
    }

    @Test
    open fun testNoSurroundingTransactionWithCommitAndRollback() {
        startProcess(WITH_COMMIT_ROLLBACK)
        assertThrows(RuntimeException::class.java) { startProcessWithRuntimeError(WITH_COMMIT_ROLLBACK) }
        // first process has finished successfully but rollback happened and we can find only one in the history
        assertEquals(1, findHistoricProcessInstances(WITH_COMMIT_ROLLBACK).size)
    }

    open fun startProcess(businessKey: String): String {
        return runtimeService.startProcessInstanceByKey("ProcessEmpty", businessKey).id
    }

    fun startProcessWithRuntimeError(businessKey: String): String {
        return runtimeService.startProcessInstanceByKey("ProcessRuntimeError", businessKey).id
    }

    fun findHistoricProcessInstances(businessKey: String): List<HistoricProcessInstance> {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .list()
    }

    companion object {
        const val WITH_COMMIT = "withCommit"
        const val WITH_ROLLBACK = "withRollback"
        const val TX_WITH_COMMIT = "surroundingTxWithCommit"
        const val TX_WITH_ROLLBACK = "surroundingTxWithRollback"
        const val WITH_COMMIT_COMMIT = "noSurroundingTxWithCommit"
        const val WITH_COMMIT_ROLLBACK = "NoSurroundingTxWithRollback"
    }
}