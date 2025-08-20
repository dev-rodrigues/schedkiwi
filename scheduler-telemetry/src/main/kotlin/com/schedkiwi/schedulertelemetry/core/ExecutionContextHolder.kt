package com.schedkiwi.schedulertelemetry.core

import java.util.concurrent.ConcurrentHashMap

/**
 * Holder ThreadLocal-safe para o contexto de execução do scheduler.
 * Garante que cada thread tenha seu próprio contexto isolado.
 * 
 * Esta classe é thread-safe e permite que múltiplos schedulers
 * executem simultaneamente sem interferência entre si.
 */
object ExecutionContextHolder {
    
    /**
     * ThreadLocal para armazenar o contexto de execução atual
     */
    private val contextHolder = ThreadLocal<ExecutionContext>()
    
    /**
     * Cache global de contextos por runId para consulta externa
     */
    private val contextCache = ConcurrentHashMap<String, ExecutionContext>()
    
    /**
     * Obtém o contexto de execução atual para a thread
     */
    fun getCurrentContext(): ExecutionContext? = contextHolder.get()
    
    /**
     * Define o contexto de execução atual para a thread
     */
    fun setCurrentContext(context: ExecutionContext) {
        contextHolder.set(context)
        contextCache[context.runId] = context
    }
    
    /**
     * Remove o contexto de execução atual da thread
     */
    fun clearCurrentContext() {
        val context = contextHolder.get()
        if (context != null) {
            contextCache.remove(context.runId)
        }
        contextHolder.remove()
    }
    
    /**
     * Obtém um contexto específico por runId
     */
    fun getContextByRunId(runId: String): ExecutionContext? = contextCache[runId]
    
    /**
     * Obtém todos os contextos ativos
     */
    fun getAllActiveContexts(): Collection<ExecutionContext> = contextCache.values
    
    /**
     * Remove um contexto específico por runId
     */
    fun removeContext(runId: String): ExecutionContext? = contextCache.remove(runId)
    
    /**
     * Verifica se existe um contexto ativo para a thread atual
     */
    fun hasCurrentContext(): Boolean = contextHolder.get() != null
    
    /**
     * Obtém o runId do contexto atual, se existir
     */
    fun getCurrentRunId(): String? = contextHolder.get()?.runId
    
    /**
     * Obtém o jobId do contexto atual, se existir
     */
    fun getCurrentJobId(): String? = contextHolder.get()?.jobId
    
    /**
     * Obtém o nome da aplicação do contexto atual, se existir
     */
    fun getCurrentAppName(): String? = contextHolder.get()?.appName
    
    /**
     * Limpa todos os contextos (útil para testes e shutdown)
     */
    fun clearAllContexts() {
        contextCache.clear()
        // Não limpa o ThreadLocal pois pode estar em uso
    }
    
    /**
     * Obtém estatísticas dos contextos ativos
     */
    fun getContextStats(): ContextStats {
        val contexts = contextCache.values
        return ContextStats(
            totalContexts = contexts.size,
            runningContexts = contexts.count { it.getStatus() == ExecutionStatus.RUNNING },
            completedContexts = contexts.count { it.getStatus() == ExecutionStatus.COMPLETED },
            failedContexts = contexts.count { it.getStatus() == ExecutionStatus.FAILED }
        )
    }
}

/**
 * Estatísticas dos contextos ativos
 */
data class ContextStats(
    val totalContexts: Int,
    val runningContexts: Int,
    val completedContexts: Int,
    val failedContexts: Int
)
