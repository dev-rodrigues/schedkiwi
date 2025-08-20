package com.schedkiwi.schedulertelemetry.aop

import com.schedkiwi.schedulertelemetry.core.ExecutionContextHolder
import com.schedkiwi.schedulertelemetry.core.ProgressTracker
import com.schedkiwi.schedulertelemetry.core.SchedulerTelemetryImpl
import com.schedkiwi.schedulertelemetry.net.MessagePriority
import com.schedkiwi.schedulertelemetry.net.OutboundMessageFactory
import com.schedkiwi.schedulertelemetry.net.SequenceDispatcher
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Aspecto AOP para instrumentação de métodos anotados com @MonitoredScheduled.
 * 
 * Este aspecto intercepta a execução de schedulers e coleta telemetria
 * de forma transparente, sem interferir no comportamento original.
 */
@Aspect
@Component
class MonitoredScheduledAspect @Autowired constructor(
    private val telemetryImpl: SchedulerTelemetryImpl,
    private val sequenceDispatcher: SequenceDispatcher
) {
    
    private val logger = LoggerFactory.getLogger(MonitoredScheduledAspect::class.java)
    
    @Value("\${spring.application.name:unknown-app}")
    private lateinit var appName: String
    
    @Value("\${server.port:8080}")
    private var serverPort: Int = 8080
    
    /**
     * Intercepta métodos anotados com @MonitoredScheduled
     */
    @Around("@annotation(monitoredScheduled)")
    fun aroundMonitoredScheduled(
        joinPoint: ProceedingJoinPoint,
        monitoredScheduled: MonitoredScheduled
    ): Any? {
        
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val className = joinPoint.target.javaClass.simpleName
        val methodName = method.name
        
        // Gera runId único para esta execução
        val runId = generateRunId(monitoredScheduled.jobId)
        
        logger.info("Iniciando monitoramento: jobId={}, runId={}, class={}, method={}", 
            monitoredScheduled.jobId, runId, className, methodName)
        
        // Cria contexto de execução
        val context = telemetryImpl.createExecutionContext(
            runId = runId,
            jobId = monitoredScheduled.jobId,
            appName = appName
        )
        
        // Adiciona metadados básicos
        context.putMetadata("class_name", className)
        context.putMetadata("method_name", methodName)
        context.putMetadata("description", monitoredScheduled.description)
        context.putMetadata("server_port", serverPort)
        
        // Adiciona metadados customizados da anotação
        addCustomMetadata(context, monitoredScheduled)
        
        // Cria ProgressTracker se habilitado
        val progressTracker = if (monitoredScheduled.enableProgressTracking) {
            ProgressTracker(context, monitoredScheduled.progressUpdateInterval)
        } else null
        
        val startTime = Instant.now()
        var endTime: Instant = Instant.now() // Inicializa com valor padrão
        var executionException: Throwable? = null
        var result: Any? = null
        
        try {
            // Executa o método original
            result = joinPoint.proceed()
            endTime = Instant.now()
            
            logger.info("Execução concluída com sucesso: jobId={}, runId={}, duration={}ms", 
                monitoredScheduled.jobId, runId, 
                java.time.Duration.between(startTime, endTime).toMillis())
                
        } catch (throwable: Throwable) {
            endTime = Instant.now()
            executionException = throwable
            
            // Adiciona exceção ao contexto
            context.addException(throwable)
            
            logger.error("Execução falhou: jobId={}, runId={}, duration={}ms, error={}", 
                monitoredScheduled.jobId, runId, 
                java.time.Duration.between(startTime, endTime).toMillis(),
                throwable.message, throwable)
            
            // Re-propaga a exceção para não alterar o comportamento
            throw throwable
            
        } finally {
            try {
                // Envia relatório final
                sendFinalReport(context, endTime, monitoredScheduled)
                
                // Finaliza contexto
                telemetryImpl.finalizeExecutionContext()
                
            } catch (e: Exception) {
                logger.error("Erro ao enviar relatório final: jobId={}, runId={}", 
                    monitoredScheduled.jobId, runId, e)
            }
        }
        
        return result
    }
    
    /**
     * Adiciona metadados customizados da anotação ao contexto
     */
    private fun addCustomMetadata(context: com.schedkiwi.schedulertelemetry.core.ExecutionContext, annotation: MonitoredScheduled) {
        // Adiciona metadados customizados
        annotation.customMetadata.forEach { metadata ->
            val parts = metadata.split("=", limit = 2)
            if (parts.size == 2) {
                context.putMetadata(parts[0].trim(), parts[1].trim())
            }
        }
        
        // Adiciona tags
        if (annotation.tags.isNotEmpty()) {
            context.putMetadata("tags", annotation.tags.toList())
        }
        
        // Adiciona configurações da anotação
        context.putMetadata("enable_progress_tracking", annotation.enableProgressTracking)
        context.putMetadata("enable_performance_metrics", annotation.enablePerformanceMetrics)
        context.putMetadata("progress_update_interval", annotation.progressUpdateInterval)
        context.putMetadata("max_item_buffer_size", annotation.maxItemBufferSize)
        context.putMetadata("message_priority", annotation.messagePriority)
        context.putMetadata("auto_register", annotation.autoRegister)
    }
    
    /**
     * Envia relatório final da execução
     */
    private fun sendFinalReport(
        context: com.schedkiwi.schedulertelemetry.core.ExecutionContext,
        endTime: Instant,
        annotation: MonitoredScheduled
    ) {
        try {
            // Cria mensagem de relatório
            val reportMessage = OutboundMessageFactory.createExecutionReportMessage(
                context = context,
                endTime = endTime,
                sequenceNumber = 1L, // TODO: Implementar numeração sequencial
                checksum = "dummy_checksum" // TODO: Implementar checksum real
            )
            
            // Determina prioridade da mensagem
            val priority = when (annotation.messagePriority.uppercase()) {
                "HIGH" -> MessagePriority.HIGH
                "LOW" -> MessagePriority.LOW
                else -> MessagePriority.NORMAL
            }
            
            // Enfileira mensagem para envio
            sequenceDispatcher.enqueueMessage(
                runId = context.runId,
                message = reportMessage,
                priority = priority
            )
            
            logger.debug("Relatório final enfileirado: runId={}, jobId={}, status={}", 
                context.runId, context.jobId, context.getStatus())
                
        } catch (e: Exception) {
            logger.error("Falha ao enfileirar relatório final: runId={}, jobId={}", 
                context.runId, context.jobId, e)
        }
    }
    
    /**
     * Gera um runId único para a execução
     */
    private fun generateRunId(jobId: String): String {
        val timestamp = System.currentTimeMillis()
        val random = UUID.randomUUID().toString().substring(0, 8)
        return "${jobId}_${timestamp}_${random}"
    }
    
    /**
     * Processa progresso em tempo real (chamado por timer se habilitado)
     */
    fun processProgressUpdate(
        context: com.schedkiwi.schedulertelemetry.core.ExecutionContext,
        progressTracker: ProgressTracker,
        annotation: MonitoredScheduled
    ) {
        try {
            // Atualiza progresso
            progressTracker.updateProgress()
            
            // Obtém informações de progresso
            val progressInfo = progressTracker.getCurrentProgress()
            
            // Cria mensagem de progresso
            val progressMessage = OutboundMessageFactory.createProgressMessage(
                context = context,
                currentItem = progressInfo.currentItem,
                currentItemMetadata = null, // TODO: Implementar metadados do item atual
                estimatedTimeRemaining = progressInfo.estimatedTimeRemaining,
                sequenceNumber = 1L, // TODO: Implementar numeração sequencial
                checksum = "dummy_checksum" // TODO: Implementar checksum real
            )
            
            // Enfileira mensagem de progresso
            sequenceDispatcher.enqueueMessage(
                runId = context.runId,
                message = progressMessage,
                priority = MessagePriority.NORMAL
            )
            
            logger.trace("Progresso atualizado: runId={}, progress={}%", 
                context.runId, progressInfo.progressPercentage)
                
        } catch (e: Exception) {
            logger.warn("Falha ao processar atualização de progresso: runId={}", 
                context.runId, e)
        }
    }
    
    /**
     * Obtém estatísticas do aspecto
     */
    fun getAspectStats(): AspectStats {
        val contextStats = ExecutionContextHolder.getContextStats()
        val queueStats = sequenceDispatcher.getQueueStats()
        
        return AspectStats(
            activeExecutions = contextStats.runningContexts,
            totalExecutions = contextStats.totalContexts,
            queuedMessages = queueStats.queueSize,
            dispatcherActive = queueStats.isActive
        )
    }
}

/**
 * Estatísticas do aspecto AOP
 */
data class AspectStats(
    val activeExecutions: Int,
    val totalExecutions: Int,
    val queuedMessages: Int,
    val dispatcherActive: Boolean
)
