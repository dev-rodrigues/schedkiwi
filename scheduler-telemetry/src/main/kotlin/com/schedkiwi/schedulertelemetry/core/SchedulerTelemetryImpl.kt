package com.schedkiwi.schedulertelemetry.core

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Implementação ThreadLocal-safe da interface SchedulerTelemetry.
 * 
 * Esta implementação utiliza o ExecutionContextHolder para garantir
 * isolamento entre threads e permite que múltiplos schedulers
 * executem simultaneamente sem interferência.
 */
@Component
class SchedulerTelemetryImpl : SchedulerTelemetry {
    
    private val logger = LoggerFactory.getLogger(SchedulerTelemetryImpl::class.java)
    
    companion object {
        /**
         * Instância singleton para acesso global
         */
        @JvmStatic
        val instance: SchedulerTelemetryImpl by lazy { SchedulerTelemetryImpl() }
    }
    
    /**
     * Define o total esperado de itens para processar nesta execução.
     */
    override fun setPlannedTotal(total: Long) {
        val context = getCurrentContextOrThrow()
        
        // Atualiza o total planejado no contexto
        val updatedContext = context.copy(plannedTotal = total)
        ExecutionContextHolder.setCurrentContext(updatedContext)
        
        logger.debug("Total planejado definido: runId={}, jobId={}, total={}", 
            context.runId, context.jobId, total)
    }
    
    /**
     * Adiciona um item processado com sucesso.
     */
    override fun addItem(key: String?, metadata: Map<String, Any?>) {
        val context = getCurrentContextOrThrow()
        
        // Incrementa contador de itens processados
        context.processedItems.incrementAndGet()
        
        // Adiciona metadados do item
        context.addItemMetadata(key, metadata, ItemOutcome.OK)
        
        logger.trace("Item adicionado: runId={}, key={}, total={}", 
            context.runId, key, context.processedItems.get())
    }
    
    /**
     * Adiciona um item que falhou durante o processamento.
     */
    override fun addFailedItem(key: String?, metadata: Map<String, Any?>, throwable: Throwable?) {
        val context = getCurrentContextOrThrow()
        
        // Incrementa contador de itens falhados
        context.failedItems.incrementAndGet()
        
        // Adiciona metadados do item
        val enrichedMetadata = metadata.toMutableMap()
        if (throwable != null) {
            enrichedMetadata["error_message"] = throwable.message ?: "Unknown error"
            enrichedMetadata["error_type"] = throwable.javaClass.simpleName
            
            // Adiciona exceção ao contexto se ainda não foi adicionada
            context.addException(throwable)
        }
        
        context.addItemMetadata(key, enrichedMetadata, ItemOutcome.ERROR)
        
        logger.debug("Item falhado adicionado: runId={}, key={}, error={}", 
            context.runId, key, throwable?.message)
    }
    
    /**
     * Adiciona um item que foi pulado (não processado).
     */
    override fun addSkippedItem(key: String?, metadata: Map<String, Any?>, reason: String?) {
        val context = getCurrentContextOrThrow()
        
        // Incrementa contador de itens pulados
        context.skippedItems.incrementAndGet()
        
        // Adiciona razão aos metadados se fornecida
        val enrichedMetadata = metadata.toMutableMap()
        if (reason != null) {
            enrichedMetadata["skip_reason"] = reason
        }
        
        context.addItemMetadata(key, enrichedMetadata, ItemOutcome.SKIPPED)
        
        logger.trace("Item pulado adicionado: runId={}, key={}, reason={}", 
            context.runId, key, reason)
    }
    
    /**
     * Adiciona uma exceção ao contexto de execução atual.
     */
    override fun addException(throwable: Throwable) {
        val context = getCurrentContextOrThrow()
        context.addException(throwable)
        
        logger.warn("Exceção adicionada ao contexto: runId={}, exception={}", 
            context.runId, throwable.message, throwable)
    }
    
    /**
     * Adiciona metadados gerais à execução atual.
     */
    override fun putMetadata(key: String, value: Any?) {
        val context = getCurrentContextOrThrow()
        context.putMetadata(key, value)
        
        logger.trace("Metadado adicionado: runId={}, key={}, value={}", 
            context.runId, key, value)
    }
    
    /**
     * Obtém metadados gerais da execução atual.
     */
    override fun getMetadata(key: String): Any? {
        val context = getCurrentContextOrThrow()
        return context.generalMetadata[key]
    }
    
    /**
     * Obtém o progresso atual da execução.
     */
    override fun getCurrentProgress(): ProgressInfo? {
        val context = ExecutionContextHolder.getCurrentContext() ?: return null
        
        return ProgressInfo(
            currentItem = context.getTotalProcessed(),
            totalItems = context.plannedTotal,
            progressPercentage = context.getProgressPercentage(),
            processedItems = context.processedItems.get(),
            failedItems = context.failedItems.get(),
            skippedItems = context.skippedItems.get(),
            estimatedTimeRemaining = null // Será calculado pelo ProgressTracker
        )
    }
    
    /**
     * Obtém o contexto de execução atual.
     */
    override fun getCurrentContext(): ExecutionContext? {
        return ExecutionContextHolder.getCurrentContext()
    }
    
    /**
     * Verifica se existe um contexto de execução ativo.
     */
    override fun hasActiveContext(): Boolean {
        return ExecutionContextHolder.hasCurrentContext()
    }
    
    /**
     * Obtém o contexto atual ou lança exceção se não existir
     */
    private fun getCurrentContextOrThrow(): ExecutionContext {
        return ExecutionContextHolder.getCurrentContext()
            ?: throw IllegalStateException(
                "Nenhum contexto de execução ativo. " +
                "Certifique-se de que o método está anotado com @MonitoredScheduled " +
                "e está sendo executado dentro de um scheduler."
            )
    }
    
    /**
     * Cria um novo contexto de execução
     */
    fun createExecutionContext(
        runId: String,
        jobId: String,
        appName: String
    ): ExecutionContext {
        val context = ExecutionContext(
            runId = runId,
            jobId = jobId,
            appName = appName
        )
        
        ExecutionContextHolder.setCurrentContext(context)
        
        logger.info("Contexto de execução criado: runId={}, jobId={}, appName={}", 
            runId, jobId, appName)
        
        return context
    }
    
    /**
     * Finaliza o contexto de execução atual
     */
    fun finalizeExecutionContext(): ExecutionContext? {
        val context = ExecutionContextHolder.getCurrentContext()
        
        if (context != null) {
            ExecutionContextHolder.clearCurrentContext()
            
            logger.info("Contexto de execução finalizado: runId={}, jobId={}, " +
                "processedItems={}, failedItems={}, skippedItems={}", 
                context.runId, context.jobId, 
                context.processedItems.get(), 
                context.failedItems.get(), 
                context.skippedItems.get())
        }
        
        return context
    }
    
    /**
     * Obtém estatísticas gerais de todos os contextos ativos
     */
    fun getGlobalStats(): GlobalTelemetryStats {
        val contextStats = ExecutionContextHolder.getContextStats()
        val allContexts = ExecutionContextHolder.getAllActiveContexts()
        
        val totalProcessedItems = allContexts.sumOf { it.processedItems.get() }
        val totalFailedItems = allContexts.sumOf { it.failedItems.get() }
        val totalSkippedItems = allContexts.sumOf { it.skippedItems.get() }
        
        return GlobalTelemetryStats(
            activeContexts = contextStats.totalContexts,
            runningContexts = contextStats.runningContexts,
            completedContexts = contextStats.completedContexts,
            failedContexts = contextStats.failedContexts,
            totalProcessedItems = totalProcessedItems,
            totalFailedItems = totalFailedItems,
            totalSkippedItems = totalSkippedItems
        )
    }
}

/**
 * Estatísticas globais de telemetria
 */
data class GlobalTelemetryStats(
    val activeContexts: Int,
    val runningContexts: Int,
    val completedContexts: Int,
    val failedContexts: Int,
    val totalProcessedItems: Long,
    val totalFailedItems: Long,
    val totalSkippedItems: Long
)

/**
 * Holder estático para acesso global à telemetria
 */
object SchedulerTelemetryHolder {
    
    /**
     * Obtém a instância de telemetria
     */
    @JvmStatic
    fun getTelemetry(): SchedulerTelemetry = SchedulerTelemetryImpl.instance
    
    /**
     * Obtém a implementação específica (para funcionalidades avançadas)
     */
    @JvmStatic
    fun getTelemetryImpl(): SchedulerTelemetryImpl = SchedulerTelemetryImpl.instance
}
