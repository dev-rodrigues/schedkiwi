package com.schedkiwi.schedulertelemetry

import com.schedkiwi.schedulertelemetry.core.SchedulerTelemetryImpl
import com.schedkiwi.schedulertelemetry.core.ExecutionContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.*

/**
 * Teste de integração básico para validar a funcionalidade da biblioteca
 */
@SpringBootTest
@TestPropertySource(properties = [
    "scheduler.telemetry.enabled=true",
    "scheduler.telemetry.manager-url=http://localhost:8080",
    "scheduler.telemetry.auth.token=test-token"
])
class SchedulerTelemetryIntegrationTest {
    
    @Autowired
    private lateinit var schedulerTelemetry: SchedulerTelemetryImpl
    
    @Test
    fun `deve criar contexto de execução`() {
        val context = schedulerTelemetry.createExecutionContext(
            jobId = "test-job",
            appName = "test-app"
        )
        
        assertNotNull(context)
        assertEquals("test-job", context.jobId)
        assertEquals("test-app", context.appName)
        assertNotNull(context.runId)
        assertNotNull(context.startTime)
    }
    
    @Test
    fun `deve definir total planejado`() {
        schedulerTelemetry.setPlannedTotal(100)
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals(100, context.plannedTotal)
    }
    
    @Test
    fun `deve adicionar item processado`() {
        schedulerTelemetry.addItem("item1", "OK")
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals(1, context.processedItems)
    }
    
    @Test
    fun `deve adicionar item com falha`() {
        schedulerTelemetry.addFailedItem("item2", "Erro de validação")
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals(1, context.failedItems)
    }
    
    @Test
    fun `deve adicionar item pulado`() {
        schedulerTelemetry.addSkippedItem("item3", "Item duplicado")
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals(1, context.skippedItems)
    }
    
    @Test
    fun `deve adicionar exceção`() {
        val exception = RuntimeException("Erro de teste")
        schedulerTelemetry.addException(exception)
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals(1, context.exceptions.size)
    }
    
    @Test
    fun `deve adicionar metadados`() {
        schedulerTelemetry.putMetadata("version", "1.0.0")
        schedulerTelemetry.putMetadata("environment", "test")
        
        val context = schedulerTelemetry.getCurrentContext()
        assertNotNull(context)
        assertEquals("1.0.0", context.getMetadata("version"))
        assertEquals("test", context.getMetadata("environment"))
    }
    
    @Test
    fun `deve calcular progresso`() {
        schedulerTelemetry.setPlannedTotal(10)
        schedulerTelemetry.addItem("item1", "OK")
        schedulerTelemetry.addItem("item2", "OK")
        schedulerTelemetry.addFailedItem("item3", "Erro")
        
        val progress = schedulerTelemetry.getCurrentProgress()
        assertNotNull(progress)
        assertEquals(3, progress.totalProcessed)
        assertEquals(2, progress.successfulItems)
        assertEquals(1, progress.failedItems)
        assertEquals(30.0, progress.percentage, 0.1)
    }
    
    @Test
    fun `deve verificar se há contexto ativo`() {
        // Inicialmente não deve haver contexto
        assertFalse(schedulerTelemetry.hasActiveContext())
        
        // Após criar contexto
        schedulerTelemetry.createExecutionContext("test-job", "test-app")
        assertTrue(schedulerTelemetry.hasActiveContext())
    }
    
    @Test
    fun `deve limpar contexto`() {
        schedulerTelemetry.createExecutionContext("test-job", "test-app")
        assertTrue(schedulerTelemetry.hasActiveContext())
        
        schedulerTelemetry.clearCurrentContext()
        assertFalse(schedulerTelemetry.hasActiveContext())
    }
}
