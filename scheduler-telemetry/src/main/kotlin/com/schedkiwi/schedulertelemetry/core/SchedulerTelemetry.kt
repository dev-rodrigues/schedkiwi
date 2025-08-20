package com.schedkiwi.schedulertelemetry.core

/**
 * Interface principal para telemetria de schedulers.
 * 
 * Esta interface fornece métodos para coletar telemetria durante a execução
 * de schedulers, incluindo progresso, metadados e exceções.
 * 
 * A implementação é ThreadLocal-safe e pode ser usada em múltiplos
 * schedulers simultaneamente sem interferência.
 */
interface SchedulerTelemetry {
    
    /**
     * Define o total esperado de itens para processar nesta execução.
     * 
     * @param total Número total de itens esperados
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun setPlannedTotal(total: Long)
    
    /**
     * Adiciona um item processado com sucesso.
     * 
     * @param key Chave opcional para identificar o item
     * @param metadata Metadados opcionais do item
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun addItem(key: String? = null, metadata: Map<String, Any?> = emptyMap())
    
    /**
     * Adiciona um item que falhou durante o processamento.
     * 
     * @param key Chave opcional para identificar o item
     * @param metadata Metadados opcionais do item
     * @param throwable Exceção que causou a falha
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun addFailedItem(key: String? = null, metadata: Map<String, Any?> = emptyMap(), throwable: Throwable? = null)
    
    /**
     * Adiciona um item que foi pulado (não processado).
     * 
     * @param key Chave opcional para identificar o item
     * @param metadata Metadados opcionais do item
     * @param reason Razão opcional para o item ter sido pulado
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun addSkippedItem(key: String? = null, metadata: Map<String, Any?> = emptyMap(), reason: String? = null)
    
    /**
     * Adiciona uma exceção ao contexto de execução atual.
     * 
     * @param throwable Exceção capturada
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun addException(throwable: Throwable)
    
    /**
     * Adiciona metadados gerais à execução atual.
     * 
     * @param key Chave do metadado
     * @param value Valor do metadado
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun putMetadata(key: String, value: Any?)
    
    /**
     * Obtém metadados gerais da execução atual.
     * 
     * @param key Chave do metadado
     * @return Valor do metadado ou null se não existir
     * @throws IllegalStateException se chamado fora de um contexto de execução
     */
    fun getMetadata(key: String): Any?
    
    /**
     * Obtém o progresso atual da execução.
     * 
     * @return Progresso atual ou null se não houver contexto ativo
     */
    fun getCurrentProgress(): ProgressInfo?
    
    /**
     * Obtém o contexto de execução atual.
     * 
     * @return Contexto atual ou null se não houver contexto ativo
     */
    fun getCurrentContext(): ExecutionContext?
    
    /**
     * Verifica se existe um contexto de execução ativo.
     * 
     * @return true se houver contexto ativo, false caso contrário
     */
    fun hasActiveContext(): Boolean
}

/**
 * Informações de progresso da execução atual
 */
data class ProgressInfo(
    val currentItem: Long,
    val totalItems: Long,
    val progressPercentage: Double,
    val processedItems: Long,
    val failedItems: Long,
    val skippedItems: Long,
    val estimatedTimeRemaining: Long? = null // em segundos
)
