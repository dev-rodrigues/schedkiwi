package com.schedkiwi.centraltelemetry.domain.entities

import com.schedkiwi.centraltelemetry.domain.valueobjects.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Testes unitários para a entidade Execution
 */
class ExecutionTest {

    @Test
    fun `deve criar Execution com valores padrão`() {
        // Given
        val runId = "run-123"
        val jobId = "job-456"
        val appName = "test-app"
        val applicationId = UUID.randomUUID()
        val scheduledJobId = UUID.randomUUID()
        val startTime = Instant.now()

        // When
        val execution = Execution(
            runId = runId,
            jobId = jobId,
            appName = appName,
            status = ExecutionStatus.RUNNING,
            startTime = startTime,
            applicationId = applicationId,
            scheduledJobId = scheduledJobId
        )

        // Then
        assertNotNull(execution.id)
        assertEquals(runId, execution.runId)
        assertEquals(jobId, execution.jobId)
        assertEquals(appName, execution.appName)
        assertEquals(ExecutionStatus.RUNNING, execution.status)
        assertEquals(startTime, execution.startTime)
        assertEquals(null, execution.endTime)
        assertEquals(0L, execution.plannedTotal)
        assertEquals(0L, execution.processedItems)
        assertEquals(0L, execution.failedItems)
        assertEquals(0L, execution.skippedItems)
        assertEquals(applicationId, execution.applicationId)
        assertEquals(scheduledJobId, execution.scheduledJobId)
        assertTrue(execution.generalMetadata.isEmpty())
        assertTrue(execution.itemMetadata.isEmpty())
        assertTrue(execution.exceptions.isEmpty())
    }

    @Test
    fun `deve criar Execution com todos os valores`() {
        // Given
        val id = UUID.randomUUID()
        val runId = "run-789"
        val jobId = "job-101"
        val appName = "full-app"
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val applicationId = UUID.randomUUID()
        val scheduledJobId = UUID.randomUUID()

        // When
        val execution = Execution(
            id = id,
            runId = runId,
            jobId = jobId,
            appName = appName,
            status = ExecutionStatus.COMPLETED,
            startTime = startTime,
            endTime = endTime,
            plannedTotal = 100L,
            processedItems = 95L,
            failedItems = 3L,
            skippedItems = 2L,
            applicationId = applicationId,
            scheduledJobId = scheduledJobId
        )

        // Then
        assertEquals(id, execution.id)
        assertEquals(runId, execution.runId)
        assertEquals(jobId, execution.jobId)
        assertEquals(appName, execution.appName)
        assertEquals(ExecutionStatus.COMPLETED, execution.status)
        assertEquals(startTime, execution.startTime)
        assertEquals(endTime, execution.endTime)
        assertEquals(100L, execution.plannedTotal)
        assertEquals(95L, execution.processedItems)
        assertEquals(3L, execution.failedItems)
        assertEquals(2L, execution.skippedItems)
        assertEquals(applicationId, execution.applicationId)
        assertEquals(scheduledJobId, execution.scheduledJobId)
    }

    @Test
    fun `deve adicionar item metadata`() {
        // Given
        val execution = createBasicExecution()
        val itemMetadata = ItemMetadata(
            key = "item-key-1",
            metadata = mapOf("field1" to "value1", "field2" to 42),
            outcome = ItemOutcome.OK,
            processingTimeMs = 150
        )

        // When
        execution.addItemMetadata(itemMetadata)

        // Then
        assertEquals(1, execution.itemMetadata.size)
        assertEquals(itemMetadata, execution.itemMetadata[0])
        assertEquals("item-key-1", execution.itemMetadata[0].key)
        assertEquals(ItemOutcome.OK, execution.itemMetadata[0].outcome)
        assertEquals(150L, execution.itemMetadata[0].processingTimeMs)
    }

    @Test
    fun `deve adicionar múltiplos item metadata`() {
        // Given
        val execution = createBasicExecution()
        val item1 = ItemMetadata(
            key = "item-1",
            outcome = ItemOutcome.OK
        )
        val item2 = ItemMetadata(
            key = "item-2",
            outcome = ItemOutcome.ERROR,
            errorMessage = "Processing failed"
        )

        // When
        execution.addItemMetadata(item1)
        execution.addItemMetadata(item2)

        // Then
        assertEquals(2, execution.itemMetadata.size)
        assertEquals("item-1", execution.itemMetadata[0].key)
        assertEquals("item-2", execution.itemMetadata[1].key)
        assertEquals(ItemOutcome.OK, execution.itemMetadata[0].outcome)
        assertEquals(ItemOutcome.ERROR, execution.itemMetadata[1].outcome)
        assertEquals("Processing failed", execution.itemMetadata[1].errorMessage)
    }

    @Test
    fun `deve adicionar exception info`() {
        // Given
        val execution = createBasicExecution()
        val exceptionInfo = ExceptionInfo(
            message = "Database connection failed",
            type = "ConnectionException",
            stackTrace = "at com.example.DatabaseService.connect()",
            severity = ExceptionSeverity.HIGH
        )

        // When
        execution.addException(exceptionInfo)

        // Then
        assertEquals(1, execution.exceptions.size)
        assertEquals(exceptionInfo, execution.exceptions[0])
        assertEquals("Database connection failed", execution.exceptions[0].message)
        assertEquals("ConnectionException", execution.exceptions[0].type)
        assertEquals(ExceptionSeverity.HIGH, execution.exceptions[0].severity)
    }

    @Test
    fun `deve adicionar múltiplas exceptions`() {
        // Given
        val execution = createBasicExecution()
        val exception1 = ExceptionInfo(
            message = "First error",
            type = "FirstException",
            severity = ExceptionSeverity.MEDIUM
        )
        val exception2 = ExceptionInfo(
            message = "Critical error",
            type = "CriticalException",
            severity = ExceptionSeverity.CRITICAL
        )

        // When
        execution.addException(exception1)
        execution.addException(exception2)

        // Then
        assertEquals(2, execution.exceptions.size)
        assertEquals("First error", execution.exceptions[0].message)
        assertEquals("Critical error", execution.exceptions[1].message)
        assertEquals(ExceptionSeverity.MEDIUM, execution.exceptions[0].severity)
        assertEquals(ExceptionSeverity.CRITICAL, execution.exceptions[1].severity)
    }

    @Test
    fun `deve adicionar metadata geral`() {
        // Given
        val execution = createBasicExecution()

        // When
        execution.putMetadata("batchSize", 1000)
        execution.putMetadata("timeout", "30s")
        execution.putMetadata("retryCount", 3)

        // Then
        assertEquals(3, execution.generalMetadata.size)
        assertEquals(1000, execution.generalMetadata["batchSize"])
        assertEquals("30s", execution.generalMetadata["timeout"])
        assertEquals(3, execution.generalMetadata["retryCount"])
    }

    @Test
    fun `deve substituir metadata existente`() {
        // Given
        val execution = createBasicExecution()
        execution.putMetadata("config", "initial")

        // When
        execution.putMetadata("config", "updated")

        // Then
        assertEquals(1, execution.generalMetadata.size)
        assertEquals("updated", execution.generalMetadata["config"])
    }

    @Test
    fun `deve funcionar com diferentes tipos de ExecutionStatus`() {
        // Given & When
        val runningExecution = createBasicExecution().copy(status = ExecutionStatus.RUNNING)
        val completedExecution = createBasicExecution().copy(status = ExecutionStatus.COMPLETED)
        val failedExecution = createBasicExecution().copy(status = ExecutionStatus.FAILED)
        val pausedExecution = createBasicExecution().copy(status = ExecutionStatus.PAUSED)

        // Then
        assertEquals(ExecutionStatus.RUNNING, runningExecution.status)
        assertEquals(ExecutionStatus.COMPLETED, completedExecution.status)
        assertEquals(ExecutionStatus.FAILED, failedExecution.status)
        assertEquals(ExecutionStatus.PAUSED, pausedExecution.status)
    }

    @Test
    fun `deve preservar imutabilidade com copy`() {
        // Given
        val originalExecution = createBasicExecution()
        
        // When
        val modifiedExecution = originalExecution.copy(
            status = ExecutionStatus.COMPLETED,
            processedItems = 100,
            endTime = Instant.now()
        )

        // Then
        assertEquals(ExecutionStatus.RUNNING, originalExecution.status)
        assertEquals(0L, originalExecution.processedItems)
        assertEquals(null, originalExecution.endTime)
        
        assertEquals(ExecutionStatus.COMPLETED, modifiedExecution.status)
        assertEquals(100L, modifiedExecution.processedItems)
        assertNotNull(modifiedExecution.endTime)
    }

    @Test
    fun `updateStatus deve ser seguro de chamar`() {
        // Given
        val execution = createBasicExecution()

        // When & Then - método não deve lançar exceção
        execution.updateStatus(ExecutionStatus.COMPLETED)

        // Status permanece inalterado pois é data class imutável
        assertEquals(ExecutionStatus.RUNNING, execution.status)
    }

    private fun createBasicExecution(): Execution {
        return Execution(
            runId = "test-run",
            jobId = "test-job",
            appName = "test-app",
            status = ExecutionStatus.RUNNING,
            startTime = Instant.now(),
            applicationId = UUID.randomUUID(),
            scheduledJobId = UUID.randomUUID()
        )
    }
}
