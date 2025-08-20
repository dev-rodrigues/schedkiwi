package com.schedkiwi.schedulertelemetry

import com.schedkiwi.schedulertelemetry.core.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.Instant
import java.util.*

class SchedulerTelemetryIntegrationTest {

    private lateinit var telemetry: SchedulerTelemetryImpl

    @BeforeEach
    fun setUp() {
        telemetry = SchedulerTelemetryImpl()
        ExecutionContextHolder.clearAllContexts()
    }

    @Test
    fun `deve criar e gerenciar contexto de execução`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val jobId = "test-job"
        val appName = "test-app"
        val plannedTotal = 100L

        // Act
        val context = ExecutionContext(
            runId = runId,
            jobId = jobId,
            appName = appName,
            plannedTotal = plannedTotal
        )
        
        ExecutionContextHolder.setCurrentContext(context)
        telemetry.setPlannedTotal(plannedTotal)

        // Assert
        val retrievedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(retrievedContext)
        assertEquals(runId, retrievedContext?.runId)
        assertEquals(jobId, retrievedContext?.jobId)
        assertEquals(appName, retrievedContext?.appName)
        assertEquals(plannedTotal, retrievedContext?.plannedTotal)
    }

    @Test
    fun `deve adicionar itens processados com sucesso`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        telemetry.addItem("item-1", mapOf("status" to "processed"))
        telemetry.addItem("item-2", mapOf("status" to "processed"))

        // Assert
        val updatedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(updatedContext)
        assertEquals(2L, updatedContext?.processedItems?.get())
        assertEquals(2, updatedContext?.itemMetadata?.size)
    }

    @Test
    fun `deve adicionar itens que falharam`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        val exception = RuntimeException("Test error")
        telemetry.addFailedItem("item-1", mapOf("status" to "failed"), exception)

        // Assert
        val updatedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(updatedContext)
        assertEquals(1L, updatedContext?.failedItems?.get())
        assertEquals(1, updatedContext?.exceptions?.size)
    }

    @Test
    fun `deve adicionar itens pulados`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        telemetry.addSkippedItem("item-1", mapOf("status" to "skipped"), "Not applicable")

        // Assert
        val updatedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(updatedContext)
        assertEquals(1L, updatedContext?.skippedItems?.get())
        assertEquals(1, updatedContext?.itemMetadata?.size)
    }

    @Test
    fun `deve adicionar metadados gerais`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        telemetry.putMetadata("environment", "test")
        telemetry.putMetadata("version", "1.0.0")

        // Assert
        val updatedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(updatedContext)
        assertEquals("test", updatedContext?.generalMetadata?.get("environment"))
        assertEquals("1.0.0", updatedContext?.generalMetadata?.get("version"))
    }

    @Test
    fun `deve calcular progresso corretamente`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val plannedTotal = 10L
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app",
            plannedTotal = plannedTotal
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        repeat(5) { i ->
            telemetry.addItem("item-$i", mapOf("index" to i))
        }

        // Assert
        val updatedContext = ExecutionContextHolder.getCurrentContext()
        assertNotNull(updatedContext)
        assertEquals(5L, updatedContext?.getTotalProcessed())
        assertEquals(50.0, updatedContext?.getProgressPercentage())
        assertFalse(updatedContext?.isComplete() ?: true)
    }

    @Test
    fun `deve finalizar execução com sucesso`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val plannedTotal = 2L
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app",
            plannedTotal = plannedTotal
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        telemetry.addItem("item-1", mapOf("status" to "processed"))
        telemetry.addItem("item-2", mapOf("status" to "processed"))
        val finalContext = telemetry.finalizeExecutionContext()

        // Assert
        assertNotNull(finalContext)
        assertEquals(ExecutionStatus.COMPLETED, finalContext!!.getStatus())
        assertTrue(finalContext!!.isComplete())
    }

    @Test
    fun `deve finalizar execução com falha`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        val exception = RuntimeException("Test error")
        telemetry.addFailedItem("item-1", mapOf("status" to "failed"), exception)
        val finalContext = telemetry.finalizeExecutionContext()

        // Assert
        assertNotNull(finalContext)
        assertEquals(ExecutionStatus.FAILED, finalContext!!.getStatus())
        assertEquals(1, finalContext!!.exceptions.size)
    }

    @Test
    fun `deve limpar contexto após finalização`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val context = ExecutionContext(
            runId = runId,
            jobId = "test-job",
            appName = "test-app"
        )
        ExecutionContextHolder.setCurrentContext(context)

        // Act
        telemetry.finalizeExecutionContext()
        ExecutionContextHolder.clearCurrentContext()

        // Assert
        assertFalse(ExecutionContextHolder.hasCurrentContext())
        assertNull(ExecutionContextHolder.getCurrentContext())
    }
}
